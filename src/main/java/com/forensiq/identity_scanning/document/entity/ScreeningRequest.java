package com.forensiq.identity_scanning.document.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScreeningRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="request_id",nullable = false,unique = true,updatable = false)
    private String requestId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScreeningStatus status;
    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime compeletedAt;
    @OneToMany(
        mappedBy = "screeningRequest",
        cascade = CascadeType.ALL,
        orphanRemoval = true
)
@JsonManagedReference
private List<Document> docDocuments;
    @PrePersist
    public void prePresiatanse(){
        if(requestId==null){
            requestId=UUID.randomUUID().toString();
        }
        if(createdAt==null){
            createdAt=LocalDateTime.now();
        }
     if(status==null){
        status=ScreeningStatus.CREATED;
     }
    }

}
