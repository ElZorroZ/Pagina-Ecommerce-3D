package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Carrito;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
public class CarritoCompletoDTO {
    private Integer id;
    private Integer productoId;
    private Integer usuarioId;
    private Integer cantidad;
    private Double precioTotal;     // Cambiado a Double
    private Double precioUnitario;  // Cambiado a Double
    private Boolean esDigital; // Cambiado a Boolean
    private String nombre;
    private String linkArchivo;

    public CarritoCompletoDTO(Integer id, Integer productoId, Integer usuarioId, Integer cantidad,
                              Double precioTotal, Double precioUnitario, Byte esDigital,
                              String nombre, String linkArchivo) {
        this.id = id;
        this.productoId = productoId;
        this.usuarioId = usuarioId;
        this.cantidad = cantidad;
        this.precioTotal = precioTotal;
        this.precioUnitario = precioUnitario;
        this.esDigital = esDigital != null ? esDigital == 1 : null; // conversión a Boolean
        this.nombre = nombre;
        this.linkArchivo = linkArchivo;
    }

}
