package com.driverkonnect.backend.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminDocumentDto {
    private Long id;
    private String documentType;
    private String fileName;
    private LocalDateTime uploadedAt;
}
