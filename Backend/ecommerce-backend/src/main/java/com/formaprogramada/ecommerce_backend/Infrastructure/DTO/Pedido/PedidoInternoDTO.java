package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Pedido;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoInternoDTO {
    Date fechaPedido;
    double total;
    String estado;
    List<ProductoEnPedidoDTOinterno> productos;
    String nombre;
    String apellido;
    String gmail;
    String direccion;
    String cp;
    String ciudad;
    String telefono;

}
