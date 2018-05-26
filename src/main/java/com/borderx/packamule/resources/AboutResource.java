package com.borderx.packamule.resources;

import com.codahale.metrics.Clock;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/v1/about")
@Produces(MediaType.APPLICATION_JSON)
public class AboutResource {
    private final Clock clock = Clock.defaultClock();
    private final long startedAt;

    public AboutResource() {
        this.startedAt = clock.getTime();
    }

    @GET
    public Response about() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode n = mapper.createObjectNode();
        n.put("name", "packmule");
        n.put("upTime", clock.getTime() - startedAt);
        return Response.status(Response.Status.OK).entity(n).build();
    }
}
