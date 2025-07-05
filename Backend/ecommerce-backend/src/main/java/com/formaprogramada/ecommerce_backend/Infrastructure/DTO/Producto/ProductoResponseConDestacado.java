package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponseConDestacado {
    private Integer id;
    private String nombre;
    private String descripcion;
    private float precio;
    private Integer categoriaId;
    private List<String> colores;
    private boolean destacado;

    public ProductoResponseConDestacado(ProductoEntity producto, boolean destacado) {
        this.id = producto.getId();
        this.nombre = producto.getNombre();
        this.descripcion = producto.getDescripcion();
        this.precio = producto.getPrecio();
        this.categoriaId = producto.getCategoriaId() != null ? producto.getCategoriaId().getId() : null;
        this.colores = colores;
        this.destacado = destacado;
    }
}
