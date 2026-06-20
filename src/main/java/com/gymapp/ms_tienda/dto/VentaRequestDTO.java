package com.gymapp.ms_tienda.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Objeto que representa la solicitud de compra de un producto por parte de un miembro")
public class VentaRequestDTO {

    @NotNull(message = "El ID del miembro es obligatorio")
    @Schema(description = "ID único del miembro en el microservicio ms-miembros", example = "150")
    private Long miembroId;

    @NotNull(message = "El ID del producto es obligatorio")
    @Schema(description = "ID del producto que se desea comprar", example = "3")
    private Long productoId;

    @Min(value = 1, message = "La cantidad mínima de compra es 1")
    @Schema(description = "Cantidad de unidades a comprar", example = "2")
    private int cantidad;
}