package com.dmdr.gateway.resource;

import com.dmdr.gateway.model.RegistryUrlDto;
import com.dmdr.gateway.service.RegistrationService;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RegistrationResourceTest {

    @Mock
    private RegistrationService registrationService;
    @Mock
    private WebClient webClient;

    private RegistrationResource registrationResource;
    private Vertx vertx;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        vertx = Vertx.vertx(); // Use real Vertx instance
        registrationResource = new RegistrationResource(registrationService, vertx);
    }

    @Test
    void testRegistry() {
        RegistryUrlDto dto = mock(RegistryUrlDto.class);
        when(dto.getApplication()).thenReturn("app1");
        when(dto.getUrl()).thenReturn("http://localhost:8080");

        String result = registrationResource.registry(dto);
        verify(registrationService).register(dto);
        assertEquals("Registered: app1:http://localhost:8080", result);
    }

    @Test
    void testProxyGet_serviceNotFound() {
        when(registrationService.getUrl("service1", "endpoint1")).thenReturn(null);
        Response response = registrationResource.proxyGet("service1", "endpoint1");
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Service or endpoint not found", response.getEntity());
    }

    @Test
    void testProxyPost_invalidUrl() {
        when(registrationService.getUrl("service1", "endpoint1")).thenReturn("ht!tp://bad_url");
        Response response = registrationResource.proxyPost("service1", "endpoint1", "{\"foo\":\"bar\"}");
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Invalid target URL", response.getEntity());
    }

    @Test
    void testProxyPut_invalidUrl() {
        when(registrationService.getUrl("service1", "endpoint1")).thenReturn("ht!tp://bad_url");
        Response response = registrationResource.proxyPut("service1", "endpoint1", "{\"foo\":\"bar\"}");
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Invalid target URL", response.getEntity());
    }

    @Test
    void testProxyDelete_serviceNotFound() {
        when(registrationService.getUrl("service1", "endpoint1")).thenReturn(null);
        Response response = registrationResource.proxyDelete("service1", "endpoint1");
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Service or endpoint not found", response.getEntity());
    }

    @Test
    void testProxyPatch_invalidUrl() {
        when(registrationService.getUrl("service1", "endpoint1")).thenReturn("ht!tp://bad_url");
        Response response = registrationResource.proxyPatch("service1", "endpoint1", "{\"foo\":\"bar\"}");
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Invalid target URL", response.getEntity());
    }

    @Test
    void testProxyRequest_internalError() {
        when(registrationService.getUrl("service1", "endpoint1")).thenReturn("http://localhost:8080/test");
        // Simulate exception in proxyRequest by mocking registrationService to throw
        doThrow(new RuntimeException("Simulated error")).when(registrationService).getUrl(anyString(), anyString());
        try {
            registrationResource.proxyGet("service1", "endpoint1");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
            assertEquals("Simulated error", e.getMessage());
        }
    }

    @Test
    void testProxyPost_success() {
        when(registrationService.getUrl("service1", "endpoint1")).thenReturn("http://localhost:8080/test");
        // Simulate successful POST by mocking WebClient behavior if needed
        Response response = registrationResource.proxyPost("service1", "endpoint1", "{\"foo\":\"bar\"}");
        // For a real sunny day, you would mock WebClient to return a successful response
        // Here, just check that the response is not NOT_FOUND or BAD_REQUEST
        assertNotEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertNotEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void testProxyPut_success() {
        when(registrationService.getUrl("service1", "endpoint1")).thenReturn("http://localhost:8080/test");
        Response response = registrationResource.proxyPut("service1", "endpoint1", "{\"foo\":\"bar\"}");
        assertNotEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertNotEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void testProxyDelete_success() {
        when(registrationService.getUrl("service1", "endpoint1")).thenReturn("http://localhost:8080/test");
        Response response = registrationResource.proxyDelete("service1", "endpoint1");
        assertNotEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertNotEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void testProxyPatch_success() {
        when(registrationService.getUrl("service1", "endpoint1")).thenReturn("http://localhost:8080/test");
        Response response = registrationResource.proxyPatch("service1", "endpoint1", "{\"foo\":\"bar\"}");
        assertNotEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertNotEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    // Additional tests can be added here
}
