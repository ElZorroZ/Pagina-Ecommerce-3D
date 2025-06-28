package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto;

import lombok.Data;

import java.util.List;

@Data
public class ProductoRequestConColores {
    private String nombre;
    private String descripcion;
    private Integer categoriaId;
    private float precio;
    private List<String> colores;

}
