package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "producto_colores_aprobacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class ProductoColorAprobacionEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;

        @ManyToOne
        @JoinColumn(name = "productId", nullable = false)
        private ProductoAprobacionEntity producto;

        @Column(length = 500)
        private String color;
}
