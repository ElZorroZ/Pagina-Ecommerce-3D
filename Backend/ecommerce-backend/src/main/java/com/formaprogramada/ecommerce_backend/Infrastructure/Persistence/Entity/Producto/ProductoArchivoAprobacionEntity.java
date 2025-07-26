package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "producto_archivos_aprobacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoArchivoAprobacionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "productId", nullable = false)
    @JsonIgnore
    private ProductoAprobacionEntity producto;

    @Column(length = 500, nullable = false)
    private byte[] archivoImagen;

    private int orden;
}
