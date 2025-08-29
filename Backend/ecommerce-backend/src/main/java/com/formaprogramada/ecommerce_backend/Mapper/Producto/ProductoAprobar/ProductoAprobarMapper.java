package com.formaprogramada.ecommerce_backend.Mapper.Producto.ProductoAprobar;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ArchivoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.*;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoResponseDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoAprobacionEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoColorAprobacionEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoColorEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoEntity;

import java.util.Base64;
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
                        a.getProducto() != null ? a.getProducto().getId() : null,
                        a.getArchivoImagen() != null ? Base64.getEncoder().encodeToString(a.getArchivoImagen()) : null,
                        a.getOrden(),
                        false
                ))
                .collect(Collectors.toList()) : List.of();



        return new ProductoAprobacionResponseDTO(
                entity.getId(),
                entity.getUsuarioId().getId(),
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getPrecio(),
                entity.getPrecioDigital(), // <-- agregado
                entity.getCategoriaId().getId(),
                colores,
                archivos,
                entity.getCodigo(),
                entity.getArchivo()

        );
    }
    public static ProductoCompletoAprobacionDTO toCompletoDTO(ProductoAprobacionEntity entity) {
        // Convertimos la parte "producto"
        ProductoAprobacionResponseDTO responseDTO = toDTO(entity);
        ProductoAprobacioDTO productoDTO = ProductoAprobarDTOMapper.fromResponseDTO(responseDTO);

        // Colores en el formato del DTO completo
        List<ColorRequest> colores = entity.getColores() != null
                ? entity.getColores().stream()
                .map(c -> new ColorRequest(c.getColor(), c.getHex()))
                .collect(Collectors.toList())
                : List.of();

        // Archivos en el formato del DTO completo
        List<ProductoAprobacionArchivoDTO> archivos = entity.getArchivos() != null
                ? entity.getArchivos().stream()
                .map(a -> new ProductoAprobacionArchivoDTO(
                        a.getId(),
                        a.getProducto() != null ? a.getProducto().getId() : null,
                        a.getArchivoImagen() != null ? Base64.getEncoder().encodeToString(a.getArchivoImagen()) : null,
                        a.getOrden(),
                        false
                ))
                .collect(Collectors.toList())
                : List.of();

        // DTO completo
        ProductoCompletoAprobacionDTO completoDTO = new ProductoCompletoAprobacionDTO();
        completoDTO.setProducto(productoDTO);
        completoDTO.setColores(colores);
        completoDTO.setArchivos(archivos);

        return completoDTO;
    }

}
