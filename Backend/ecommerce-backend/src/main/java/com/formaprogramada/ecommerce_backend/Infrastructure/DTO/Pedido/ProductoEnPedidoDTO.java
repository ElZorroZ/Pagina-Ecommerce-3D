package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Pedido;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoEnPedidoDTO {
    private Integer id;
    private Integer productoId;
    private String nombre;
    private double precio;
    private int cantidad;
    private Boolean esDigital;
    private Integer colorId;
    private String colorNombre;
    private String hex;
    private double precioTotal;
    // NUEVO: archivo en Base64
    private String archivoBase64;
    private String imagen;
}
