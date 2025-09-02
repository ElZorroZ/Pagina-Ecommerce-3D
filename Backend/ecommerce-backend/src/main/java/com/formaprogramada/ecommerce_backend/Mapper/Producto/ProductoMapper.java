package com.formaprogramada.ecommerce_backend.Mapper.Producto;

import com.formaprogramada.ecommerce_backend.Domain.Model.Producto.Producto;
import com.formaprogramada.ecommerce_backend.Domain.Model.Producto.ProductoArchivo;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ArchivoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoResponseDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoColorEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
@Component

public class ProductoMapper {

    public static ProductoResponseDTO toDTO(ProductoEntity entity) {
        List<String> colores = entity.getColores() != null
                ? entity.getColores().stream()
                .map(ProductoColorEntity::getColor)
                .collect(Collectors.toList())
                : List.of();

        List<ArchivoDTO> archivos = entity.getArchivos() != null
                ? entity.getArchivos().stream()
                .map(a -> new ArchivoDTO(
                        a.getId(),
                        a.getProducto() != null ? a.getProducto().getId() : null, // productoId
                        a.getLinkArchivo(),
                        a.getOrden()
                ))
                .collect(Collectors.toList())
                : List.of();


        return new ProductoResponseDTO(
                entity.getId(),
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getPrecio(),
                colores,
                archivos
        );
    }
    public static Producto toModel(ProductoEntity entity) {
        Producto producto = new Producto();
        producto.setId(entity.getId());
        producto.setNombre(entity.getNombre());
        producto.setDescripcion(entity.getDescripcion());
        producto.setPrecio(entity.getPrecio());
        producto.setCategoriaId(entity.getCategoriaId() != null ? entity.getCategoriaId().getId() : null);

        // Mapear archivos a modelo de dominio si tu ProductoArchivo lo necesita
        List<ProductoArchivo> archivos = entity.getArchivos() != null
                ? entity.getArchivos().stream()
                .map(a -> new ProductoArchivo(
                        a.getId(),
                        a.getProducto() != null ? a.getProducto().getId() : null, // productoId
                        a.getLinkArchivo(),
                        a.getOrden()
                ))
                .collect(Collectors.toList())
                : List.of();

        producto.setArchivos(archivos);

        return producto;
    }


}