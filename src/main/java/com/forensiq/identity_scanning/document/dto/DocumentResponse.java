package com.forensiq.identity_scanning.document.dto;


import java.time.LocalDateTime;

import com.forensiq.identity_scanning.document.entity.DocumentStatus;
import com.forensiq.identity_scanning.document.entity.DocumentType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentResponse {
 private Long id;

    private String fileName;

    private String mimeType;

    private Long fileSize;

    private DocumentType documentType;

    private DocumentStatus status;

    private LocalDateTime uploadedAt;

    private String requestId;
}
