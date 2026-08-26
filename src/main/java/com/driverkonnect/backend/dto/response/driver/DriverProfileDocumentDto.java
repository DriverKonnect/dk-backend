package com.driverkonnect.backend.dto.response.driver;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DriverProfileDocumentDto {

    private String documentType;
    private String fileName;
    private String filePath;
    private LocalDateTime uploadedAt;
}
