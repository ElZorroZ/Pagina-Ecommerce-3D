package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categoria_destacado")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CategoriaDestacadoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "categoriaId", nullable = false)
    private CategoriaEntity categoria;

}
