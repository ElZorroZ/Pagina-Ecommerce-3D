package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Carrito;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DescuentoDTO {
    private Integer id;
    private String nombre;
    private String descripcion;
    private Double porcentaje;
}
