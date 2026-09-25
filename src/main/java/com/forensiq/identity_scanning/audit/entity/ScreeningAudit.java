package com.forensiq.identity_scanning.audit.entity;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table(name="screening_audits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreeningAudit {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false)
    private Long documentId;
    @Column(nullable=false)
    private String requestId;
    @Column(nullable=false)
    private String documentHash;
    @Column(nullable=false)
    private String documentType;
    @Column(nullable=false)
    private double riskScore;
    @Column(nullable=false)
    private String verdict;
    @Column(nullable=false)
    private String decisionConfidence;
    @Column(nullable=false)
    private int checksPerformed;
    @Column(nullable=false)
    private int checksAvailable;
    @Column(columnDefinition="TEXT")
    private String reasons;
    @Column(columnDefinition="TEXT")
    private String warnings;
    @Column(nullable=false)
    private LocalDateTime createdAt;
    private String ipAddress;
    @Column(nullable=false,length=128)
    private String auditHash;
    @Column(length=128)
    private String previousAuditHash;
    @PrePersist
    public void prePersist(){
        if(createdAt==null){
            createdAt=LocalDateTime.now();
        }
    }
}