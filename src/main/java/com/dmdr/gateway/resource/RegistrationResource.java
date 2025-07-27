package com.dmdr.gateway.resource;

import com.dmdr.gateway.model.RegistryUrlDto;
import com.dmdr.gateway.service.RegistrationService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;

import io.vertx.core.http.HttpMethod;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/")
public class RegistrationResource {

    private final RegistrationService registrationService;
    private final WebClient webClient;

    @Inject
    public RegistrationResource(RegistrationService registrationService, Vertx vertx) {
        this.registrationService = registrationService;
        this.webClient = WebClient.create(vertx);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("registry")
    public String registry(RegistryUrlDto request) {
        registrationService.register(request);
        log.info("Registered {} : {}", request.getApplication(), request.getUrl());
        return "Registered: " + request.getApplication() + ":" + request.getUrl();
    }

    @GET
    @Path("{service}/{endpoint}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response proxyGet(
            @PathParam("service") String service,
            @PathParam("endpoint") String endpoint) {
        return proxyRequest(HttpMethod.GET, service, endpoint, null);
    }

    @POST
    @Path("{service}/{endpoint}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response proxyPost(
            @PathParam("service") String service,
            @PathParam("endpoint") String endpoint,
            String body) {
        return proxyRequest(HttpMethod.POST, service, endpoint, body);
    }

    @PUT
    @Path("{service}/{endpoint}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response proxyPut(
            @PathParam("service") String service,
            @PathParam("endpoint") String endpoint,
            String body) {
        return proxyRequest(HttpMethod.PUT, service, endpoint, body);
    }

    @DELETE
    @Path("{service}/{endpoint}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response proxyDelete(
            @PathParam("service") String service,
            @PathParam("endpoint") String endpoint) {
        return proxyRequest(HttpMethod.DELETE, service, endpoint, null);
    }

    @PATCH
    @Path("{service}/{endpoint}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response proxyPatch(
            @PathParam("service") String service,
            @PathParam("endpoint") String endpoint,
            String body) {
        return proxyRequest(HttpMethod.PATCH, service, endpoint, body);
    }

    private Response proxyRequest(HttpMethod method, String service, String endpoint, String body) {
        String targetUrl = registrationService.getUrl(service, endpoint);
        if (targetUrl == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Service or endpoint not found").build();
        }
        URI uri;
        try {
            uri = new java.net.URI(targetUrl);
        } catch (Exception e) {
            log.error("Invalid target URL: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid target URL").build();
        }
        int port = uri.getPort() == -1 ? 8080 : uri.getPort();
        String host = uri.getHost();
        String path = uri.getRawPath();
        log.info("{} {}:{}{}", method, host, port, path);
        try {
            var req = webClient.request(method, port, host, path);
            if (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH) {
                if (body == null) {
                    return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Request body is required for this method.").build();
                }
                req = req.putHeader("Content-Type", MediaType.APPLICATION_JSON);
                String responseBody = req.sendBuffer(io.vertx.mutiny.core.buffer.Buffer.buffer(body))
                        .await().indefinitely()
                        .bodyAsString();
                return Response.ok(responseBody).build();
            } else {
                String responseBody = req.sendAndAwait().bodyAsString();
                return Response.ok(responseBody).build();
            }
        } catch (Exception e) {
            log.error("Error proxying {} to {}:{}{}: {}", method, host, port, path, e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Proxy error: " + e.getMessage()).build();
        }
    }

}
