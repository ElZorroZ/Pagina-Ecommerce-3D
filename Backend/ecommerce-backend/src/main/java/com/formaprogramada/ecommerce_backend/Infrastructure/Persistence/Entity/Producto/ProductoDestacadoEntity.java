package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "producto_destacado")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoDestacadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "producto_id", nullable = false, unique = true)
    private ProductoEntity producto;
}
