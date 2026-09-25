package com.forensiq.identity_scanning.information.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.forensiq.identity_scanning.information.model.ExtractedDocumentField;
@Repository
public interface ExtractedDocumentFieldRepo extends JpaRepository<ExtractedDocumentField,Long>{

void deleteByDocument_Id(Long documentId);
List<ExtractedDocumentField> findByDocumentId(Long id);
}
