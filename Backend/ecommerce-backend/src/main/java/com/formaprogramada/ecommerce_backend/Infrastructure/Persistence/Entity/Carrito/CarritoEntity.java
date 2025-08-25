package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Carrito;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name ="carrito")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CarritoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 100, nullable = false)
    private int productoId;

    @Column(length = 100, nullable = false)
    private int usuarioId;

    @Column(length = 100, nullable = false)
    private int cantidad;

    @Column(length = 100, nullable = false)
    private double precioTotal;

    @Column(length = 100, nullable = false)
    private double precioUnitario;

    @Column(length = 100, nullable = true)
    private int colorId;

    @Column(columnDefinition = "TINYINT")
    private boolean esDigital;


}
