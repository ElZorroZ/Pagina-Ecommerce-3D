package com.formaprogramada.ecommerce_backend.Domain.Model;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    private Integer id;
    private String nombre;
    private String apellido;
    private String gmail;
    private String password;
    private Boolean permiso;
    private boolean verificado = false;
}

