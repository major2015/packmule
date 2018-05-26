package com.borderx.packamule.exceptions;

import com.borderx.packamule.util.ObjectMappers;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class Errors {

    /**
     * <code>code</code> and <code>messages</code> must have the same length.
     */
    private List<String> errors;
    private List<String> messages;

    @JsonProperty
    public List<String> getErrors() {
        return this.errors;
    }

    @JsonProperty
    public List<String> getMessages() {
        return this.messages;
    }

    public void add(String e) {
        add(e, "");
    }

    public void add(String e, String m) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
            this.messages = new ArrayList<>();
        }
        this.errors.add(e);
        this.messages.add(m);
    }

    public boolean noError() {
        return this.errors == null || this.errors.isEmpty();
    }

    public static Errors create(String error) {
        Errors e = new Errors();
        e.add(error);
        return e;
    }

    public static Errors create(String error, String message) {
        Errors e = new Errors();
        e.add(error, message);
        return e;
    }

    public Response toResponse() {
        return toResponse(Response.Status.BAD_REQUEST);
    }

    public Response toResponse(Response.Status status) {
        if (this.noError()) {
            return Response.status(status).build();
        }
        ObjectNode r = ObjectMappers.get().createObjectNode();

        ArrayNode errArray = ObjectMappers.get().createArrayNode();
        this.errors.stream().forEach(e -> errArray.add(e));
        r.set("errors", errArray);

        ArrayNode msgArray = ObjectMappers.get().createArrayNode();
        this.messages.stream().forEach(e -> msgArray.add(e));
        r.set("messages", msgArray);

        return Response.status(status).entity(r).build();
    }

    @Override
    public String toString() {
        if (this.messages == null) {
            return this.errors.toString();
        } else {
            return this.errors.toString() + "; " + this.messages.toString();
        }
    }
}
