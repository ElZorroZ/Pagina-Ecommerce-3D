package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Auth;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
public class AuthRequest {

    @NotBlank(message = "El gmail es obligatorio")
    @Email(message = "El gmail debe ser válido")
    @Size(max = 200, message = "El gmail puede tener hasta 200 caracteres")
    private String gmail;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 16, message = "La contraseña debe tener entre 8 y 16 caracteres")
    private String password;
}
