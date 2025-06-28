package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categoria")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CategoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 100, nullable = false)
    private String nombre;

    @Column(length = 300)
    private String descripcion;
}
