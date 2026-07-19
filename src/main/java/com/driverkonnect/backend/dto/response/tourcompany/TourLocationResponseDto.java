package com.driverkonnect.backend.dto.response.tourcompany;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TourLocationResponseDto {
    private Long id;
    private String locationType;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer sequenceOrder;
}
