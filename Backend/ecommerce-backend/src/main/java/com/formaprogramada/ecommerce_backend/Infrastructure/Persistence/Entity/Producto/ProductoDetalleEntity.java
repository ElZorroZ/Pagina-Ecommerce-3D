package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "producto_detalle")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoDetalleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String dimension;

    private String material;

    private String tecnica;

    private String peso;

    @Column(name = "productoId")
    private Integer productoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productoId", insertable = false, updatable = false)
    private ProductoEntity producto;
}
