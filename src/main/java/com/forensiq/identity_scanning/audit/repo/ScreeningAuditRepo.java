package com.forensiq.identity_scanning.audit.repo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.forensiq.identity_scanning.audit.entity.ScreeningAudit;
@Repository
public interface ScreeningAuditRepo extends JpaRepository<ScreeningAudit,Long> {
    Optional<ScreeningAudit> findTopByDocumentIdOrderByCreatedAtDesc(Long documentId);
    Optional<ScreeningAudit> findTopByOrderByCreatedAtDesc();
}