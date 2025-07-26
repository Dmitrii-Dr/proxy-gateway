package com.dmdr.gateway.resource;

import com.dmdr.gateway.model.RegistryUrlDto;
import com.dmdr.gateway.service.RegistrationService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
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

    @Inject
    RegistrationService registrationService;

    private final WebClient webClient = WebClient.create(Vertx.vertx());

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
        String targetUrl = registrationService.getUrl(service, endpoint);
        if (targetUrl == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Service or endpoint not found").build();
        }
        // Parse and set port 8080 if not present
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
        log.info("GET {}:{}{}", host, port, path);
        try {
            String responseBody = webClient.request(HttpMethod.GET, port, host, path)
                    .sendAndAwait()
                    .bodyAsString();
            return Response.ok(responseBody).build();
        } catch (Exception e) {
            log.error("Error proxying request to {}:{}{}: {}", host, port, path, e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Proxy error: " + e.getMessage()).build();
        }
    }

}
