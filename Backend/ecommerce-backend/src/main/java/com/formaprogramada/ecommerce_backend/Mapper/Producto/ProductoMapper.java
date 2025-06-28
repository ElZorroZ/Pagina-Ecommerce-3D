package com.formaprogramada.ecommerce_backend.Mapper.Producto;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ArchivoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoResponseDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoColorEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoEntity;

import java.util.List;
import java.util.stream.Collectors;

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
}