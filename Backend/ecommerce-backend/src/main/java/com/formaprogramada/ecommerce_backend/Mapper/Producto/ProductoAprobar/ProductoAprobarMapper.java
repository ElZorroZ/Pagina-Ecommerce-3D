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
        // Colores
        List<String> colores = entity.getColores() != null
                ? entity.getColores().stream()
                .map(ProductoColorAprobacionEntity::getColor)
                .collect(Collectors.toList())
                : List.of();

        // Archivos
        List<ProductoAprobacionArchivoDTO> archivos = entity.getArchivos() != null
                ? entity.getArchivos().stream()
                .map(a -> new ProductoAprobacionArchivoDTO(
                        a.getId(),
                        a.getProducto() != null ? a.getProducto().getId() : null,
                        a.getArchivoImagen() != null
                                ? Base64.getEncoder().encodeToString(a.getArchivoImagen())
                                : null,
                        a.getOrden(),
                        false
                ))
                .collect(Collectors.toList())
                : List.of();

        // Nombre completo del usuario
        String nombreUsuario = "Desconocido";
        Integer usuarioId = null;
        if (entity.getUsuarioId() != null) {
            usuarioId = entity.getUsuarioId().getId();
            // Forzar carga LAZY
            nombreUsuario = entity.getUsuarioId().getNombre() + " " + entity.getUsuarioId().getApellido();
        }

        return new ProductoAprobacionResponseDTO(
                entity.getId(),
                usuarioId,
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getPrecio(),
                entity.getPrecioDigital(),
                entity.getCategoriaId().getId(),
                colores,
                archivos,
                entity.getCodigo(),
                entity.getArchivo(),
                nombreUsuario  // <-- agregar aquí
        );
    }

    public static ProductoCompletoAprobacionDTO toCompletoDTO(ProductoAprobacionEntity entity) {
        ProductoAprobacionResponseDTO responseDTO = toDTO(entity);
        ProductoAprobacioDTO productoDTO = ProductoAprobarDTOMapper.fromResponseDTO(responseDTO);

        // Colores en formato DTO completo
        List<ColorRequest> colores = entity.getColores() != null
                ? entity.getColores().stream()
                .map(c -> new ColorRequest(c.getColor(), c.getHex()))
                .collect(Collectors.toList())
                : List.of();

        // Archivos en formato DTO completo
        List<ProductoAprobacionArchivoDTO> archivos = entity.getArchivos() != null
                ? entity.getArchivos().stream()
                .map(a -> new ProductoAprobacionArchivoDTO(
                        a.getId(),
                        a.getProducto() != null ? a.getProducto().getId() : null,
                        a.getArchivoImagen() != null
                                ? Base64.getEncoder().encodeToString(a.getArchivoImagen())
                                : null,
                        a.getOrden(),
                        false
                ))
                .collect(Collectors.toList())
                : List.of();

        ProductoCompletoAprobacionDTO completoDTO = new ProductoCompletoAprobacionDTO();
        completoDTO.setProducto(productoDTO);
        completoDTO.setColores(colores);
        completoDTO.setArchivos(archivos);

        return completoDTO;
    }
}
