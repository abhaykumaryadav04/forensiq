package com.forensiq.identity_scanning.result.repo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.forensiq.identity_scanning.result.model.ScreeningResult;

@Repository
public interface ScreeningResultRepo extends JpaRepository<ScreeningResult,Long> {
    Optional<ScreeningResult> findByDocumentId(Long documentId);
    Optional<ScreeningResult> findByDocumentHash(String documentHash);
}