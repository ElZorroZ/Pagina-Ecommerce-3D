package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "producto_aprobacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"archivos", "colores", "usuarioId", "categoriaId"})
@ToString(exclude = {"archivos", "colores"})
public class ProductoAprobacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuarioCreador")
    private UsuarioEntity usuarioId;

    @Column(length = 20, nullable = false)
    private String nombre;

    @Column(length = 15)
    private String codigo;

    @Column(length = 80)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoriaId")
    private CategoriaEntity categoriaId;

    private float precio;

    private float precioDigital;

    @Lob
    @Column(name = "archivo", columnDefinition = "LONGBLOB")
    private byte[] archivo;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductoArchivoAprobacionEntity> archivos = new HashSet<>();

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductoColorAprobacionEntity> colores = new HashSet<>();
}