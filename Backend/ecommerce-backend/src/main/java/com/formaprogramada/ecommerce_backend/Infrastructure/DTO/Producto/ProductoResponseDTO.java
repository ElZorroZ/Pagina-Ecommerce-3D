package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoResponseDTO {
    private Integer id;
    private String nombre;
    private String descripcion;
    private float precio;
    private List<String> colores;
    private List<ArchivoDTO> archivos;
}
