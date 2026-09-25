package com.forensiq.identity_scanning.document.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.forensiq.identity_scanning.document.entity.DocumentPage;

@Repository
public interface DocumentPageRepo extends JpaRepository<DocumentPage,Long>{
  List<DocumentPage> findByDocumentIdOrderByPageNumber(Long id);
}
