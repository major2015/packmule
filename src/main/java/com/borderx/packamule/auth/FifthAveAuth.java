package com.borderx.packamule.auth;

import com.borderx.packamule.PackmuleConfiguration;
import com.borderx.packamule.util.ObjectMappers;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FifthAveAuth implements IAuth {

    private static final Logger logger = LoggerFactory.getLogger(FifthAveAuth.class);

    public static final String SESSION_USER_HEADER = "X-Session-User";
    public static final String SESSION_KEY_HEADER = "X-Session-Key";

    private final HttpClient httpClient;
    private final PackmuleConfiguration.FifthAve config;
    private final Cache<String, List<String>> userCapabilities;

    public FifthAveAuth(HttpClient httpClient, PackmuleConfiguration.FifthAve config) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(config.getUrl())
                && !Strings.isNullOrEmpty(config.getAdminUserId())
                && !Strings.isNullOrEmpty(config.getAdminSessionKey()));
        this.httpClient = httpClient;
        this.config = config;
        this.userCapabilities = CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build();
    }

    @Override
    public boolean check(HttpServletRequest httpReq, @Nullable Capability capability) {
        String userId = httpReq.getHeader(SESSION_USER_HEADER);
        String sessionKey = httpReq.getHeader(SESSION_KEY_HEADER);
        if (Strings.isNullOrEmpty(userId) || Strings.isNullOrEmpty(sessionKey)) {
            return false;
        }
        // Special case: the incoming request knows our secret!
        if (userId.equals(config.getAdminUserId())
                && sessionKey.equals(config.getAdminSessionKey())) {
            return true;
        }

        String cacheKey = userId + ":" + sessionKey;
        List<String> capabilities = this.userCapabilities.getIfPresent(cacheKey);
        if (capabilities != null) {
            return capability == null || capabilities.contains(capability.name());
        }

        if (!authN(userId, sessionKey)) {
            return false;
        }
        capabilities = retrieveCapabilities(userId);
        if (capabilities == null) {  // null signals unexpected failure
            return false;
        }
        this.userCapabilities.put(cacheKey, capabilities);
        return capability == null || capabilities.contains(capability.name());
    }

    private boolean authN(String userId, String sessionKey) {
        String url = StringUtils.stripEnd(config.getUrl(), "/");
        url += "/api/v1/profile/public-info";
        HttpGet request = new HttpGet(url);
        request.addHeader(SESSION_USER_HEADER, userId);
        request.addHeader(SESSION_KEY_HEADER, sessionKey);
        try {
            HttpResponse resp = httpClient.execute(request);
            EntityUtils.consume(resp.getEntity());
            return resp.getStatusLine().getStatusCode() == Response.Status.OK.getStatusCode();
        } catch (IOException e) {
            logger.info("Unexpected error while authenticating user {}:", userId, e);
        }
        return false;
    }

    private List<String> retrieveCapabilities(String userId) {
        String url = StringUtils.stripEnd(config.getUrl(), "/");
        url += "/api/v1/_internal/users/" + userId + "/role";
        HttpGet request = new HttpGet(url);
        request.addHeader(SESSION_USER_HEADER, config.getAdminUserId());
        request.addHeader(SESSION_KEY_HEADER, config.getAdminSessionKey());
        try {
            HttpResponse resp = httpClient.execute(request);
            int statusCode = resp.getStatusLine().getStatusCode();
            if (statusCode != Response.Status.OK.getStatusCode()) {
                logger.warn("Failed to retrieve capabilities of {}: {}",
                        userId, resp.getStatusLine());
                return null;
            }
            String body = EntityUtils.toString(resp.getEntity());
            Role role = ObjectMappers.mustReadValue(body, Role.class);
            return role.capabilities == null ? Lists.newArrayList() : role.capabilities;
        } catch (IOException e) {
            logger.warn("Unexpected error while retrieving user capability: ", e);
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Role {
        @JsonProperty
        List<String> capabilities;
    }
}
