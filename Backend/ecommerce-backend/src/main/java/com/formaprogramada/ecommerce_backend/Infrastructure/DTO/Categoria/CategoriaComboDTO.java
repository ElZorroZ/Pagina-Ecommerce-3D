package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Categoria;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoriaComboDTO {
    private Integer id;
    private String nombre;
    private boolean destacado;
    private String linkArchivo;
}
