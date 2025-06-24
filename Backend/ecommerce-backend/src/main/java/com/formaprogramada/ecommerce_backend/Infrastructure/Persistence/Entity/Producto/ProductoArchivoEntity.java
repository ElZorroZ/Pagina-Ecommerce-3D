package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "producto_archivos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoArchivoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "productId", nullable = false)
    private ProductoEntity producto;

    @Column(length = 500, nullable = false)
    private String linkArchivo;

    private int orden;
}

