package com.borderx.packamule.exceptions;

import com.google.common.collect.ImmutableMap;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class ResourceNotFoundException extends WebApplicationException {

    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String resource) {
        super(Response
                .status(Response.Status.NOT_FOUND)
                .entity(ImmutableMap.of("resource", resource, "_code", 404))
                .build());
    }
}
