package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ArchivoDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoCompletoAprobacionDTO {
    private ProductoAprobacioDTO producto;
    private List<ColorRequest> colores;
    private List<ProductoAprobacionArchivoDTO> archivos;
}
