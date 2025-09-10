package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Pedido;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDTO {
    private Integer id;
    private Date fechaPedido;
    private double total;
    private String estado;
    private int usuarioId;
    private List<ProductoEnPedidoDTO> productos;
}
