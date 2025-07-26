package com.formaprogramada.ecommerce_backend.Mapper.Producto.ProductoAprobar;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ArchivoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ProductoAprobacionArchivoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ProductoAprobacionArchivoResponse;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoArchivoResponse;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoArchivoAprobacionEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoArchivoEntity;

public class ArchivoAprobarMapper {

    public static ProductoAprobacionArchivoResponse toResponseDTO(ProductoArchivoAprobacionEntity entity) {
        if (entity == null) return null;
        return new ProductoAprobacionArchivoResponse(entity);
    }


    public static ProductoAprobacionArchivoDTO toArchivoDTO(ProductoArchivoAprobacionEntity entity) {
        if (entity == null) return null;
        return new ProductoAprobacionArchivoDTO(
                entity.getId(),
                entity.getProducto() != null ? entity.getProducto().getId() : null,
                entity.getArchivoImagen(),
                entity.getOrden()
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
