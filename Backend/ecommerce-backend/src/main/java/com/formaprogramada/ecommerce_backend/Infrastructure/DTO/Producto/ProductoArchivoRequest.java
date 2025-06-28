package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoArchivoRequest {
    private Integer productId;
    private String linkArchivo;
    private int orden;
}
