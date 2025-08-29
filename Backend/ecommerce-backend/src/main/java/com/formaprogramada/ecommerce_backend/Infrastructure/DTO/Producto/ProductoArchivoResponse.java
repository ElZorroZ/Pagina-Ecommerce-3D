package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoArchivoAprobacionEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoArchivoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoArchivoResponse {
    private Integer id;
    private String url;
    private int orden;
    private String error;

    // Constructor para archivos aprobados
    public ProductoArchivoResponse(ProductoArchivoEntity entity) {
        this.id = entity.getId();
        this.url = entity.getLinkArchivo();
        this.orden = entity.getOrden();
        this.error = null;
    }

    // Constructor para archivos de aprobación (nuevo)
    public ProductoArchivoResponse(ProductoArchivoAprobacionEntity entity) {
        this.id = entity.getId();
        this.url = null; // o el campo que uses si manejás URL en aprobación
        this.orden = entity.getOrden();
        this.error = null;
    }

    // Constructor para errores
    public ProductoArchivoResponse(String error) {
        this.error = error;
        this.id = null;
        this.url = null;
        this.orden = 0;
    }
}
