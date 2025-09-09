package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Pedido;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoEnPedidoDTOinterno {
    private Integer id;
    private String nombre;
    private double precioTotal;
    private int cantidad;
    private Boolean esDigital;
    private int idProducto;
}
