package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Descuento;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarritoAgregarRequest {

    @NotNull(message = "El id de producto es obligatorio")
    @DecimalMin(value = "0", inclusive = false)
    private int productoId;

    @NotNull(message = "El id de usuario es obligatorio")
    @DecimalMin(value = "0", inclusive = false)
    private int usuarioId;

    @NotNull(message = "la cantidad es obligatoria")
    @DecimalMin(value = "0", inclusive = false)
    private int cantidad;

    @DecimalMin(value = "0", inclusive = false)
    private int precioTotal;


    @DecimalMin(value = "0", inclusive = false)
    private int precioUnitario;

}
