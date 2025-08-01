package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ProductoAprobacionArchivoDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoConArchivoPrincipalYColoresDTO {
    private ProductoDTO producto;
    private ProductoAprobacionArchivoDTO archivoPrincipal;
    private List<String> colores;
}
