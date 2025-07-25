package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoAprobacionArchivoDTO {
    private Integer id;
    private Integer productId;
    private byte[] archivoImagen;
    private Integer orden;

    public ProductoAprobacionArchivoDTO(Integer id, Integer integer, byte[] archivoImagen) {
    }
}
