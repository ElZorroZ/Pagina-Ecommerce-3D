package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "producto_colores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoColorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "productId", nullable = false)
    private ProductoEntity producto;

    @Column(length = 500)
    private String color;

    @Column(length = 9) // máximo #RRGGBB
    private String hex;
}
