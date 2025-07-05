package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Categoria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaDTOconImagen {
    private Integer id;
    private String nombre;
    private String descripcion;
    private boolean destacada;
    private String linkArchivo;
}

