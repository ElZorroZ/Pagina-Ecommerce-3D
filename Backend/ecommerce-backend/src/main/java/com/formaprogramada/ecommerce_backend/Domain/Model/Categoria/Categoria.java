package com.formaprogramada.ecommerce_backend.Domain.Model.Categoria;
import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Categoria {
    private Integer id;
    private String nombre;
    private String descripcion;

}
