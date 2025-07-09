package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Descuento;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "descuentos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class DescuentoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 100, nullable = false)
    private String nombre;

    @Column(length = 300)
    private String descripcion;

    @Column(length = 40)
    private double porcentaje;
}
