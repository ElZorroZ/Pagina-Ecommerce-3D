package com.formaprogramada.ecommerce_backend.Mapper.Producto.ProductoAprobar;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ProductoAprobacioDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ProductoAprobacionResponseDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoResponseDTO;
import lombok.*;

import java.util.Arrays;

@Data

public class ProductoAprobarDTOMapper {
    public static ProductoAprobacioDTO fromResponseDTO(ProductoAprobacionResponseDTO responseDTO) {
        if (responseDTO == null) return null;

        ProductoAprobacioDTO dto = new ProductoAprobacioDTO();
        dto.setId(responseDTO.getId());
        dto.setUsuarioId(responseDTO.getIdCreador());
        dto.setNombre(responseDTO.getNombre());
        dto.setDescripcion(responseDTO.getDescripcion());
        dto.setCategoriaId(null); // si no está en ResponseDTO
        dto.setPrecio(responseDTO.getPrecio());
        dto.setArchivo(responseDTO.getArchivos() != null && !responseDTO.getArchivos().isEmpty()
                ? Arrays.toString(responseDTO.getArchivos().get(0).getArchivoImagen())
                : null);

        return dto;
    }
}
