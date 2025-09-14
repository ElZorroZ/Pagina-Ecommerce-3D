package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Carrito;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarritoProductoDTO {
    private int idProducto;
    private String nombreProducto;
    private int cantidad;
    private Integer colorId; // nuevo

}
