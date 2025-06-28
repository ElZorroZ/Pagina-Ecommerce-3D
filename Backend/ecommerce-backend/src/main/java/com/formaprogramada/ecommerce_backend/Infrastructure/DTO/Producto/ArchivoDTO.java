package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArchivoDTO {
    private Integer id;
    private Integer productId;
    private String linkArchivo;
    private Integer orden;
}
