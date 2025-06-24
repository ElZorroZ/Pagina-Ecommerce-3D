package com.formaprogramada.ecommerce_backend.Domain.Model.Producto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoArchivo {

    private Integer id;
    private Integer productId;
    private String linkArchivo;
    private int orden;
}
