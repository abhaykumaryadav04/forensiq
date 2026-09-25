package com.forensiq.identity_scanning.ocr.entity;

import com.forensiq.identity_scanning.document.entity.DocumentPage;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
public class OcrWordEntity {
  @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    private Float confidence;

    private Integer x;

    private Integer y;

    private Integer width;

    private Integer height;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "document_page_id",
            nullable = false
    )
    private DocumentPage documentPage;
}
