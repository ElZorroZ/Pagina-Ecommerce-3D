package com.formaprogramada.ecommerce_backend.Mapper.Producto.ProductoAprobar;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ArchivoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ProductoAprobacionArchivoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ProductoAprobacionResponseDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoResponseDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoAprobacionEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoColorAprobacionEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoColorEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoEntity;

import java.util.List;
import java.util.stream.Collectors;


public class ProductoAprobarMapper {
    public static ProductoAprobacionResponseDTO toDTO(ProductoAprobacionEntity entity) {
        List<String> colores = entity.getColores() != null
                ? entity.getColores().stream()
                .map(ProductoColorAprobacionEntity::getColor)
                .collect(Collectors.toList())
                : List.of();

        List<ProductoAprobacionArchivoDTO> archivos = entity.getArchivos() != null
                ? entity.getArchivos().stream()
                .map(a -> new ProductoAprobacionArchivoDTO(
                        a.getId(),
                        a.getProducto() != null ? a.getProducto().getId() : null, // productoId
                        a.getArchivoImagen(),
                        a.getOrden()
                ))
                .collect(Collectors.toList()) : List.of();


        return new ProductoAprobacionResponseDTO(
                entity.getId(),
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getPrecio(),
                colores,
                archivos
        );
    }
}
