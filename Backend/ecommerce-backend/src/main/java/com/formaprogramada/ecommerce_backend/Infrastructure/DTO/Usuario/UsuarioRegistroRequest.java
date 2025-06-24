package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario;
import jakarta.validation.constraints.*;
import lombok.*;
@Data
@Builder
public class UsuarioRegistroRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre puede tener hasta 100 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido puede tener hasta 100 caracteres")
    private String apellido;

    @NotBlank(message = "El gmail es obligatorio")
    @Email(message = "El gmail debe ser válido")
    @Size(max = 200, message = "El gmail puede tener hasta 200 caracteres")
    private String gmail;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 16, message = "La contraseña debe tener entre 8 y 16 caracteres")
    private String password;

}
