package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Pedido;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDTO {
    Integer id;
    Date fechaPedido;
    double total;
    int usuarioId;
    String estado;
}
