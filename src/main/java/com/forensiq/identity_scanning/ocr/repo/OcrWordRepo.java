package com.forensiq.identity_scanning.ocr.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.forensiq.identity_scanning.ocr.entity.OcrWordEntity;

public interface OcrWordRepo extends JpaRepository<OcrWordEntity,Long>{
List<OcrWordEntity> findByDocumentPageId(Long id);
    
} 
