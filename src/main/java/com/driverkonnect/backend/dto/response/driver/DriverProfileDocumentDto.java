package com.driverkonnect.backend.dto.response.driver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DriverProfileDocumentDto {

    private String documentType;
    private String fileName;
    private String filePath;
    private LocalDateTime uploadedAt;
}
