package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Carrito;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoColorEntity;
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

    @Column(columnDefinition = "TINYINT")
    private boolean esDigital;

    @ManyToOne
    @JoinColumn(name = "colorId", referencedColumnName = "id", nullable = true)
    private ProductoColorEntity color;


}
