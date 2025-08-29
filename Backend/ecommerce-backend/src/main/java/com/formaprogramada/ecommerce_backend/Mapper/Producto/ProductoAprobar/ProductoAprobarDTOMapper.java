package com.formaprogramada.ecommerce_backend.Mapper.Producto.ProductoAprobar;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ProductoAprobacioDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ProductoAprobacionResponseDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoResponseDTO;
import lombok.*;

import java.util.Arrays;
import java.util.Base64;

@Data

public class ProductoAprobarDTOMapper {
    public static ProductoAprobacioDTO fromResponseDTO(ProductoAprobacionResponseDTO responseDTO) {
        if (responseDTO == null) return null;

        ProductoAprobacioDTO dto = new ProductoAprobacioDTO();
        dto.setId(responseDTO.getId());
        dto.setUsuarioId(responseDTO.getIdCreador());
        dto.setNombre(responseDTO.getNombre());
        dto.setDescripcion(responseDTO.getDescripcion());
        dto.setCategoriaId(responseDTO.getCategoriaId());

        dto.setPrecio(responseDTO.getPrecio());

        // Convertir byte[] a Base64 String para archivo ZIP/STL
        if (responseDTO.getArchivo() != null) {
            dto.setArchivo(Base64.getEncoder().encodeToString(responseDTO.getArchivo()));
        } else {
            dto.setArchivo(null);
        }

        dto.setCodigoInicial(responseDTO.getCodigo());

        return dto;
    }

}

