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

    @NotBlank(message = "La descripcion es obligatoria")
    @Size(max = 300, message = "La descripcion puede tener hasta 300 caracteres")
    private String descripcion;
}
