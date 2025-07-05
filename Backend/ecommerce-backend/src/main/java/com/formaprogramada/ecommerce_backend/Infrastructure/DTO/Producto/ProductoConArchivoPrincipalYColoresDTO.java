package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoConArchivoPrincipalYColoresDTO {
    private ProductoDTO producto;
    private ArchivoDTO archivoPrincipal;
    private List<String> colores;
}
