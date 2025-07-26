package com.dmdr.gateway.model.db;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "registry_url")
@AllArgsConstructor
@NoArgsConstructor
public class RegistryUrlEntity extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true)
    public String externalId;

    @Column(nullable = false)
    public String application;

    @Column(nullable = false)
    public String host;

    @Column(nullable = false)
    public String url;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
}
