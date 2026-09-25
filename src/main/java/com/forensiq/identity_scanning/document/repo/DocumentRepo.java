package com.forensiq.identity_scanning.document.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.forensiq.identity_scanning.document.entity.Document;

@Repository
public interface DocumentRepo extends JpaRepository<Document,Long>{
    boolean existsByFileHash(String fileHash);
    Optional<Document> findByFileHash(String fileHash);

}
