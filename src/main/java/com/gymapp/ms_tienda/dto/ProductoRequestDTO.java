package com.gymapp.ms_tienda.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Objeto para la creación o actualización de un producto en el catálogo")
public class ProductoRequestDTO {

    @NotBlank(message = "El nombre del producto no puede estar vacío")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    @Schema(description = "Nombre comercial del producto", example = "Proteína Whey 1kg")
    private String nombre;

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    @Schema(description = "Descripción detallada", example = "Sabor chocolate, rápida absorción")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Schema(description = "Precio unitario del producto", example = "35000.00")
    private BigDecimal precio;

    @Min(value = 0, message = "El stock no puede ser negativo")
    @Schema(description = "Cantidad inicial en inventario", example = "50")
    private int stock;
}
