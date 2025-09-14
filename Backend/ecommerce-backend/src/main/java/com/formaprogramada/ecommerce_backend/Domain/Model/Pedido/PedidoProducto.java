package com.formaprogramada.ecommerce_backend.Domain.Model.Pedido;


import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class PedidoProducto {
    private Integer id;
    private String nombre;
    private Integer pedidoId;
    private Integer productoId;
    private Integer cantidad;
    private Double precio;
    private int colorId;
    private String colorNombre;
    private Boolean esDigital;
}
