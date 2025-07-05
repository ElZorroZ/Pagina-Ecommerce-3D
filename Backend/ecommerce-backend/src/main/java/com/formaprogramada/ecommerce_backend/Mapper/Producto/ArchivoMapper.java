package com.formaprogramada.ecommerce_backend.Mapper.Producto;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ArchivoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoArchivoResponse;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoArchivoEntity;

public class ArchivoMapper {

    // Método que devuelve ProductoArchivoResponse
    public static ProductoArchivoResponse toResponseDTO(ProductoArchivoEntity entity) {
        if (entity == null) return null;
        return new ProductoArchivoResponse(entity);
    }

    // Método que devuelve ArchivoDTO (IMPORTANTE para tu caso)
    public static ArchivoDTO toArchivoDTO(ProductoArchivoEntity entity) {
        if (entity == null) return null;
        return new ArchivoDTO(
                entity.getId(),
                entity.getProducto() != null ? entity.getProducto().getId() : null,
                entity.getLinkArchivo(),
                entity.getOrden()
        );
    }

    public static ProductoArchivoEntity toEntity(ProductoArchivoResponse dto) {
        if (dto == null) return null;

        ProductoArchivoEntity entity = new ProductoArchivoEntity();
        entity.setId(dto.getId());
        entity.setLinkArchivo(dto.getUrl());
        entity.setOrden(dto.getOrden());

        return entity;
    }

}
