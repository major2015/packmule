package com.borderx.packamule.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URI;

public class InvalidArgumentException extends WebApplicationException {
    private static final long serialVersionUID = 1L;

    private Errors errors;

    public InvalidArgumentException() {
        this(null, null);
    }

    public InvalidArgumentException(URI location) {
        this(location, null);
    }

    public InvalidArgumentException(String message) {
        this(Errors.create(message));
    }

    public InvalidArgumentException(URI location, Object entity) {
        super(Response.status(Status.BAD_REQUEST).location(location).entity(entity).build());
    }

    public InvalidArgumentException(Errors errors) {
        super(Response.status(Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(errors)
                .build());
        this.errors = errors;
    }

    public Errors getErrors() {
        return this.errors;
    }

    @Override
    public String getMessage() {
        if (errors != null) {
            return errors.getErrors() != null ? errors.getErrors().toString() : "";
        }
        return super.getMessage();
    }
}
