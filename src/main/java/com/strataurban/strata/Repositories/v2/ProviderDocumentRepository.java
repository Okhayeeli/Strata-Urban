package com.strataurban.strata.Repositories.v2;

import com.strataurban.strata.Entities.Providers.ProviderDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderDocumentRepository extends JpaRepository<ProviderDocument, String> {

    // Find provider documents by provider ID (assuming a providerId field is added to ProviderDocument)
    List<ProviderDocument> findAllByProviderId(Long providerId);
    Optional<ProviderDocument> findByProviderId(Long providerId);
}