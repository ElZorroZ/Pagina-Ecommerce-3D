package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categoria_archivos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CategoriaArchivoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "categoriaId", nullable = false)
    @JsonIgnore
    private CategoriaEntity categoriaId;

    @Column(length = 500, nullable = false)
    private String linkArchivo;

    @Column(length = 500)
    private String deleteUrl;

}
