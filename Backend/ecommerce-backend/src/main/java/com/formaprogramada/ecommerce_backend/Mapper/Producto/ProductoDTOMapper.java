package com.formaprogramada.ecommerce_backend.Mapper.Producto;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoResponseDTO;

public class ProductoDTOMapper {
    public static ProductoDTO fromResponseDTO(ProductoResponseDTO responseDTO) {
        if (responseDTO == null) return null;

        ProductoDTO dto = new ProductoDTO();
        dto.setId(responseDTO.getId());
        dto.setNombre(responseDTO.getNombre());
        dto.setDescripcion(responseDTO.getDescripcion());
        dto.setCategoriaId(null); // si no está en ResponseDTO
        dto.setPrecio(responseDTO.getPrecio());
        dto.setArchivo(responseDTO.getArchivos() != null && !responseDTO.getArchivos().isEmpty()
                ? responseDTO.getArchivos().get(0).getLinkArchivo()
                : null);

        return dto;
    }

}