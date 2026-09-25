package com.forensiq.identity_scanning.classification.entity;

import com.forensiq.identity_scanning.document.entity.DocumentPage;
import com.forensiq.identity_scanning.document.entity.DocumentType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Entity
@Builder
@AllArgsConstructor
@Data
public class DocumentClassification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JoinColumn(unique = true,nullable = false,name = "document_page_ids")
    private DocumentPage documentPage;
    private long processTime;
    private double confidence;
    @Enumerated(EnumType.STRING)
    private DocumentType documentType;
    private String modelVersion;

}
