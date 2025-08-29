package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ColorRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoCompletoDTO {
    private ProductoDTO producto;
    private List<ColorRequest> colores;
    private List<ArchivoDTO> archivos;
}
