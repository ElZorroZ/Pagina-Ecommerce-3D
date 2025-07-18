package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import lombok.*;
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UsuarioUpdatePedido {

    @Size(max = 100, message = "El nombre no puede tener más de 100 caracteres")
    @Nullable
    private String nombre;

    @Size(max = 100, message = "El apellido no puede tener más de 100 caracteres")
    @Nullable
    private String apellido;

    @Size(max = 200, message = "El gmail no puede tener más de 200 caracteres")
    @Nullable
    private String gmail;

    @Size(max = 200, message = "La dirección no puede tener más de 200 caracteres")
    @Nullable
    private String direccion;

    @Size(max = 20, message = "El código postal no puede tener más de 20 caracteres")
    @Pattern(regexp = "(^$|^[a-zA-Z0-9\\s-]{3,20}$)", message = "Código postal inválido")
    @Nullable
    private String cp;

    @Size(max = 100, message = "La ciudad no puede tener más de 100 caracteres")
    @Nullable
    private String ciudad;

    @Size(max = 20, message = "El teléfono no puede tener más de 20 caracteres")
    @Pattern(regexp = "(^$|\\d{1,20})", message = "El teléfono debe contener hasta 20 dígitos")
    @Nullable
    private String telefono;
}