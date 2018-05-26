package com.borderx.packamule;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.HttpClientConfiguration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class PackmuleConfiguration extends Configuration {
    @Valid
    @NotNull
    @JsonProperty("database")
    private DataSourceFactory database = new DataSourceFactory();

    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    @Valid
    @NotNull
    @JsonProperty("httpClient")
    private HttpClientConfiguration httpClient = new HttpClientConfiguration();

    public HttpClientConfiguration getHttpClientConfiguration() {
        return httpClient;
    }

    public static class FifthAve {

        @JsonProperty
        private String url;

        @JsonProperty
        private String adminUserId;

        @JsonProperty
        private String adminSessionKey;

        public String getUrl() {
            return url;
        }

        public String getAdminUserId() {
            return adminUserId;
        }

        public String getAdminSessionKey() {
            return adminSessionKey;
        }
    }

    @Valid
    @NotNull
    @JsonProperty
    private FifthAve fifthAve = new FifthAve();

    public FifthAve getFifthAve() {
        return fifthAve;
    }
}
