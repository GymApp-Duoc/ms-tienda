package com.gymapp.ms_tienda.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data @Builder
public class VentaResponseDTO {
    private Long ventaId;
    private String producto;
    private BigDecimal total;
    private String estado;
}