package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoPaginadoDTO {
    private List<ProductoConArchivoPrincipalYColoresDTO> productos;
    private int paginaActual;
    private int totalPaginas;
    private long totalElementos;
}
