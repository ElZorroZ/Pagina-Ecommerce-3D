package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequest {
    private String nombre;
    private String descripcion;
    private Long categoriaId;
    private float precio;
    private List<String> colores;
}
