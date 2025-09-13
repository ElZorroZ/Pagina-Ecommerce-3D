package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Categoria;
import jakarta.validation.constraints.*;
import lombok.*;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaCrearRequest {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre puede tener hasta 100 caracteres")
    private String nombre;
}
