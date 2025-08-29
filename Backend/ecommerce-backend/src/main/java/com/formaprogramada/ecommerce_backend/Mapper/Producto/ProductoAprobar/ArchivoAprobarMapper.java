package com.formaprogramada.ecommerce_backend.Mapper.Producto.ProductoAprobar;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ArchivoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ProductoAprobacionArchivoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ProductoAprobacionArchivoResponse;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoArchivoResponse;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoArchivoAprobacionEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoArchivoEntity;

import java.util.Base64;

public class ArchivoAprobarMapper {

    public static ProductoAprobacionArchivoResponse toResponseDTO(ProductoArchivoAprobacionEntity entity) {
        if (entity == null) return null;
        return new ProductoAprobacionArchivoResponse(entity);
    }


    public static ProductoAprobacionArchivoDTO toArchivoDTO(ProductoArchivoAprobacionEntity entity) {
        if (entity == null) return null;

        String archivoImagen = null;
        if (entity.getArchivoImagen() != null) {
            archivoImagen = Base64.getEncoder().encodeToString(entity.getArchivoImagen());
        }

        return new ProductoAprobacionArchivoDTO(
                entity.getId(),
                entity.getProducto() != null ? entity.getProducto().getId() : null,
                archivoImagen, // aquí mandamos la imagen en base64
                entity.getOrden(),
                false
        );
    }

    public static ProductoArchivoAprobacionEntity toEntity(ProductoAprobacionArchivoResponse dto) {
        if (dto == null) return null;

        ProductoArchivoAprobacionEntity entity = new ProductoArchivoAprobacionEntity();
        entity.setId(dto.getId());
        entity.setArchivoImagen(dto.getArchivoImagen());
        entity.setOrden(dto.getOrden());

        return entity;
    }
}
