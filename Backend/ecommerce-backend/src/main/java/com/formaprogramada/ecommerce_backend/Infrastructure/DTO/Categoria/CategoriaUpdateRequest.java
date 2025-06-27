package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Categoria;

import jakarta.annotation.Nullable;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaUpdateRequest {
    @Nullable
    @Size(max = 100, message = "El nombre puede tener hasta 100 caracteres")
    private String nombre;

    @Nullable
    @Size(max = 300, message = "La descripcion puede tener hasta 300 caracteres")
    private String descripcion;

}
