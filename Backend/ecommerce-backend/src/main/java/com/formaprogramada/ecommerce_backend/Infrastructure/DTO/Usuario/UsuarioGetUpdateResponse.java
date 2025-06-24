package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioGetUpdateResponse {
    private String nombre;
    private String apellido;
    private String gmail;
    private String password;
    private String direccion;
    private String cp;      // código postal
    private String ciudad;
    private String telefono;
}

