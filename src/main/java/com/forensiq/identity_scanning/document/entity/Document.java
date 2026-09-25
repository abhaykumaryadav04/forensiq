package com.forensiq.identity_scanning.document.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.forensiq.identity_scanning.information.model.ExtractedDocumentField;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
   @Column(nullable = false)
    private String fileName;
    @Column(nullable = false)
    private String mimeType;
    @Column(nullable = false)
    private Long fileSize;
    @Column(nullable = false, unique = true)
    private String fileHash;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;
    @Column(nullable = false)
    private LocalDateTime uploadedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn( name = "screening_request_id",nullable = false)
    private ScreeningRequest screeningRequest;
    @Column(nullable = false)
    private String storagePath;
    @OneToMany(mappedBy = "document",orphanRemoval = true)
    @JsonBackReference
    private List<DocumentPage> pages;
    @OneToMany( mappedBy = "document", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<ExtractedDocumentField> extrctedDocumentFields;
}
