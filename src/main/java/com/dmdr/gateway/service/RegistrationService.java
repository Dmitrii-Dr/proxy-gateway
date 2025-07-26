package com.dmdr.gateway.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import com.dmdr.gateway.model.RegistryUrlDto;
import com.dmdr.gateway.model.db.RegistryUrlEntity;
import com.dmdr.gateway.repository.RegistryUrlRepository;
import jakarta.inject.Inject;
import java.time.LocalDateTime;

@ApplicationScoped
public class RegistrationService {
    @Inject
    RegistryUrlRepository repository;

    @Transactional
    public void register(RegistryUrlDto request) {
        String url = request.getUrl();
        String externalId = buildId(request.getApplication(), url);
        String formattedUrl = url.startsWith("/") ? url.substring(1) : url;

        RegistryUrlEntity entity = new RegistryUrlEntity(null,
                externalId,
                request.getApplication(),
                request.getHost(),
                formattedUrl,
                LocalDateTime.now());

        repository.persistOrUpdateByExternalId(entity);
    }

    // TODO Add index
    public String getUrl(String host, String url) {
        String externalId = buildId(host, url);
        RegistryUrlEntity entity = repository.findByExternalId(externalId);
        if (entity != null) {
            String formattedHost = entity.host.startsWith("http://") || entity.host.startsWith("https://") 
                ? entity.host 
                : "http://" + entity.host;
            String formattedUrl = entity.url.startsWith("/") ? entity.url : "/" + entity.url;
            return formattedHost + formattedUrl;
        }
        return null;
    }

    private String buildId(String application, String url) {
        String formattedUrl = url.startsWith("/") ? url.substring(1) : url;
        return application + "." + formattedUrl.replaceAll("/", ".");
    }
}