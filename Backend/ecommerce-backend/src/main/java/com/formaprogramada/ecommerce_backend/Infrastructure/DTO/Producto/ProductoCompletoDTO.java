package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoCompletoDTO {
    private ProductoDTO producto;
    private List<String> colores;
    private List<ArchivoDTO> archivos;
}
