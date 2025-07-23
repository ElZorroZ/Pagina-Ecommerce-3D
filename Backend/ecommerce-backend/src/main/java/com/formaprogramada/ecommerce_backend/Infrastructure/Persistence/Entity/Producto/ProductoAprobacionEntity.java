package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "producto_aprobacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoAprobacionEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuarioCreador")  // el nombre de la columna FK en la tabla producto
    private UsuarioEntity usuarioId;

    @Column(length = 20, nullable = false)
    private String nombre;

    @Column(length = 15)
    private String codigo;

    @Column(length = 80)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoriaId")  // el nombre de la columna FK en la tabla producto
    private CategoriaEntity categoriaId;

    private float precio;

    @Lob
    @Column(name = "archivo", columnDefinition = "LONGBLOB")
    private byte[] archivo;  // STL

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductoArchivoAprobacionEntity> archivos;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductoColorAprobacionEntity> colores = new ArrayList<>();
}

