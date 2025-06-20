package com.formaprogramada.ecommerce_backend.Domain.Model;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {
    private Integer id;
    private String nombre;
    private String descripcion;
}
