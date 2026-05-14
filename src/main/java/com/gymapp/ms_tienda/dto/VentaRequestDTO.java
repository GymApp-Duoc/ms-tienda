package com.gymapp.ms_tienda.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VentaRequestDTO {
    @NotNull(message = "Producto requerido")
    private Long productoId;

    @NotNull(message = "Miembro requerido")
    private Long miembroId;

    @Min(value = 1, message = "Mínimo 1 unidad")
    private int cantidad;
}