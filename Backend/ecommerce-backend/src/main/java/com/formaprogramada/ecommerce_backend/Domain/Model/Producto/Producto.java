package com.formaprogramada.ecommerce_backend.Domain.Model.Producto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    private Integer id;
    private String nombre;
    private String descripcion;
    private Integer categoriaId;
    private float precio;
    private List<ProductoArchivo> archivos;
}

