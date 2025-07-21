package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "producto_colores_aprobacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
