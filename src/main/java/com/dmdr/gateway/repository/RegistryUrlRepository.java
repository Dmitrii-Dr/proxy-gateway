package com.dmdr.gateway.repository;

import com.dmdr.gateway.model.db.RegistryUrlEntity;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RegistryUrlRepository implements PanacheRepository<RegistryUrlEntity> {
    /**
     * Upsert (insert or update) RegistryUrlEntity by externalId using native query.
     */
    @jakarta.transaction.Transactional
    public void persistOrUpdateByExternalId(RegistryUrlEntity entity) {
        getEntityManager().createNativeQuery(
            "INSERT INTO registry_url (externalId, application, host, url, created_at) " +
            "VALUES (?, ?, ?, ?, ?) " +
            "ON CONFLICT (externalId) DO UPDATE SET " +
            "application = EXCLUDED.application, " +
            "host = EXCLUDED.host, " +
            "url = EXCLUDED.url, " +
            "created_at = EXCLUDED.created_at"
        )
        .setParameter(1, entity.externalId)
        .setParameter(2, entity.application)
        .setParameter(3, entity.host)
        .setParameter(4, entity.url)
        .setParameter(5, entity.createdAt)
        .executeUpdate();
    }
    
    /**
     * Find a RegistryUrlEntity by its externalId.
     *
     * @param externalId the external ID to search for
     * @return the found RegistryUrlEntity or null if not found
     */
    public RegistryUrlEntity findByExternalId(String externalId) {
        return find("externalId", externalId).firstResult();
    }

}
