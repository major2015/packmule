package com.borderx.packamule.exceptions;

import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class AuthFailedException extends WebApplicationException {

    private static final long serialVersionUID = 1L;

    private static final String CHALLENGE = String.format(
            "Session realm=\"%s\"", "BorderX Lab Bieyang");

    public AuthFailedException(Response.Status status, String entity) {
        this(status, entity, MediaType.TEXT_PLAIN_TYPE);
    }

    public AuthFailedException(Response.Status status, ObjectNode entity) {
        this(status, entity, MediaType.APPLICATION_JSON_TYPE);
    }

    public AuthFailedException(Response.Status status, Object entity, MediaType mediaType) {
        super(Response.status(status)
                .header(HttpHeaders.WWW_AUTHENTICATE, CHALLENGE)
                .entity(entity)
                .type(mediaType)
                .build());
    }

    public AuthFailedException(Errors errors) {
        super(Response.status(Response.Status.UNAUTHORIZED)
                .type(MediaType.APPLICATION_JSON)
                .entity(errors)
                .build());
    }
}
