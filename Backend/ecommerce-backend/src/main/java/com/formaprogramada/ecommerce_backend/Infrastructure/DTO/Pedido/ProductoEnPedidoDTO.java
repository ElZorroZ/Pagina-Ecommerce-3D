package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Pedido;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoEnPedidoDTO {
    private Integer id;
    private String nombre;
    private double precioTotal;
    private int cantidad;
}
