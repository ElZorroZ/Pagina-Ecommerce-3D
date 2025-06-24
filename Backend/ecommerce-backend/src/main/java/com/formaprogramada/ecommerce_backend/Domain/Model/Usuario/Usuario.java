package com.formaprogramada.ecommerce_backend.Domain.Model.Usuario;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    private Integer id;
    private Boolean permiso;
    private String nombre;
    private String apellido;
    private String gmail;
    private String password;
    private boolean verificado = false;
    private String direccion;
    private String cp;      // código postal
    private String ciudad;
    private String telefono;
}

