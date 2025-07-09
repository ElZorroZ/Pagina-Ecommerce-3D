package com.formaprogramada.ecommerce_backend.Domain.Model.Descuento;

import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Descuento {
    private Integer id;
    private String nombre;
    private String descripcion;
    private Double porcentaje;
}
