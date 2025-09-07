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
    private Double precioTotal;
    private Double precioUnitario;
    private Integer colorId;
    private Integer esDigital;
    private String nombre;
    private String linkArchivo;

    // Nuevos campos de color
    private String colorNombre;
    private String colorHex;
    public CarritoCompletoDTO() {
        // Constructor vacío necesario para Jackson
    }
    public CarritoCompletoDTO(Integer id, Integer productoId, String nombre,
                              Integer usuarioId, Integer cantidad, Double precioTotal,
                              Double precioUnitario, Integer colorId, Object esDigital,
                              String linkArchivo, String colorNombre) {
        this.id = id;
        this.productoId = productoId;
        this.nombre = nombre;
        this.usuarioId = usuarioId;
        this.cantidad = cantidad;
        this.precioTotal = precioTotal;
        this.precioUnitario = precioUnitario;
        this.colorId = colorId;

        if (esDigital instanceof Byte) {
            this.esDigital = ((Byte) esDigital).intValue();
        } else if (esDigital instanceof Integer) {
            this.esDigital = (Integer) esDigital;
        } else if (esDigital != null) {
            this.esDigital = Integer.valueOf(esDigital.toString());
        } else {
            this.esDigital = null;
        }

        this.linkArchivo = linkArchivo;
        this.colorNombre = colorNombre;
    }

    public CarritoCompletoDTO(Integer id, Integer productoId, Integer usuarioId, Integer cantidad,
                              Double precioTotal, Double precioUnitario, Integer colorId,
                              Object esDigital, String linkArchivo, String colorNombre) {
        this.id = id;
        this.productoId = productoId;
        this.usuarioId = usuarioId;
        this.cantidad = cantidad;
        this.precioTotal = precioTotal;
        this.precioUnitario = precioUnitario;
        this.colorId = colorId;

        if (esDigital instanceof Byte) {
            this.esDigital = ((Byte) esDigital).intValue();
        } else if (esDigital instanceof Integer) {
            this.esDigital = (Integer) esDigital;
        } else if (esDigital != null) {
            this.esDigital = Integer.valueOf(esDigital.toString());
        } else {
            this.esDigital = null;
        }

        this.linkArchivo = linkArchivo;
        this.colorNombre = colorNombre;
    }
    // Constructor actualizado
    public CarritoCompletoDTO(Integer id, Integer productoId, Integer usuarioId, Integer cantidad,
                              Double precioTotal, Double precioUnitario, Integer colorId,
                              Object esDigital, String nombre, String linkArchivo,
                              String colorNombre, String colorHex) {
        this.id = id;
        this.productoId = productoId;
        this.usuarioId = usuarioId;
        this.cantidad = cantidad;
        this.precioTotal = precioTotal;
        this.precioUnitario = precioUnitario;
        this.colorId = colorId;

        // Manejo de esDigital
        if (esDigital instanceof Byte) {
            this.esDigital = ((Byte) esDigital).intValue();
        } else if (esDigital instanceof Integer) {
            this.esDigital = (Integer) esDigital;
        } else if (esDigital != null) {
            this.esDigital = Integer.valueOf(esDigital.toString());
        } else {
            this.esDigital = null;
        }

        this.nombre = nombre;
        this.linkArchivo = linkArchivo;
        this.colorNombre = colorNombre;
        this.colorHex = colorHex;
    }

    public Boolean getEsDigitalBoolean() {
        return this.esDigital != null && this.esDigital == 1;
    }

    public BigDecimal getPrecioTotalAsBigDecimal() {
        return precioTotal != null ? BigDecimal.valueOf(precioTotal) : null;
    }

    public BigDecimal getPrecioUnitarioAsBigDecimal() {
        return precioUnitario != null ? BigDecimal.valueOf(precioUnitario) : null;
    }
}
