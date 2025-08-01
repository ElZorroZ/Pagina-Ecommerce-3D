package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "producto_archivos_aprobacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
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
