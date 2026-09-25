package com.forensiq.identity_scanning.document.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.forensiq.identity_scanning.document.entity.ScreeningRequest;

@Repository
public interface ScreeningRequestRepo extends JpaRepository<ScreeningRequest,Long> {
Optional<ScreeningRequest> findByRequestId(String requestId);
}
