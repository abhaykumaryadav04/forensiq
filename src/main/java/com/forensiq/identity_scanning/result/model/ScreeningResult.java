package com.forensiq.identity_scanning.result.model;
import java.time.LocalDateTime;
import com.forensiq.identity_scanning.document.entity.Document;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table(name="screening_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreeningResult {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn(name="document_id",nullable=false,unique=true)
    private Document document;
    @Column(nullable=false,length=128)
    private String documentHash;
    @Column(nullable=false)
    private double riskScore;
    @Column(nullable=false,length=50)
    private String verdict;
    @Column(nullable=false,length=50)
    private String decisionConfidence;
    @Column(nullable=false)
    private int checksPerformed;
    @Column(nullable=false)
    private int checksAvailable;
    @Column(nullable=false,columnDefinition="TEXT")
    private String resultJson;
    @Column(nullable=false)
    private LocalDateTime createdAt;
    @Column(nullable=false)
    private LocalDateTime updatedAt;
}