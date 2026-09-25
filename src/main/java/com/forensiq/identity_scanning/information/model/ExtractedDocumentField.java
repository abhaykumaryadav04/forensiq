package com.forensiq.identity_scanning.information.model;

import com.forensiq.identity_scanning.document.entity.Document;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExtractedDocumentField {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "document_id",nullable = false)
    private Document document;
    @Enumerated(EnumType.STRING)
    private ExtractedFieldType extractedFieldType;
    private double confidence;
    private String value;
    @Enumerated(EnumType.STRING)
    private ExtractedSource extractedSource;


}
