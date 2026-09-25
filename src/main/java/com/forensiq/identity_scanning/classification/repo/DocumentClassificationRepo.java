package com.forensiq.identity_scanning.classification.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.forensiq.identity_scanning.classification.entity.DocumentClassification;

@Repository
public interface DocumentClassificationRepo
        extends JpaRepository<DocumentClassification, Long> {

    Optional<DocumentClassification> findByDocumentPageId(Long documentPageId);

}