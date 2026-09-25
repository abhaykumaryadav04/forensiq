package com.forensiq.identity_scanning.document.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.forensiq.identity_scanning.ocr.entity.OcrWordEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentPage {
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Integer pageNumber;
    private Integer width;
    private Integer height;
    private Integer dpi;
    private Long ocrProcessingTimeMs;
    private Double ocrConfidence;
    private String imagepath;
    @Enumerated(EnumType.STRING)
    private PageStatus status;
    @ManyToOne
    @JoinColumn(name = "document_id",nullable = false)
    @JsonBackReference
    private Document document;
     @Column(columnDefinition = "TEXT")
    private String ocrText;
    @OneToMany(
        mappedBy = "documentPage",
        cascade = CascadeType.ALL,
        orphanRemoval = true
)
@JsonManagedReference
private List<OcrWordEntity> ocrWords ;
        

}
