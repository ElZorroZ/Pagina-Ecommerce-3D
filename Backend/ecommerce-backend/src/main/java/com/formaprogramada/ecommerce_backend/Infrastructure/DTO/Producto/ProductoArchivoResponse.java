package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto;

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


    public ProductoArchivoResponse(ProductoArchivoEntity entity) {
        this.id = entity.getId();
        this.url = entity.getLinkArchivo();  // acá el nombre correcto
        this.orden = entity.getOrden();
        this.error = null;  // porque acá no hay error
    }
    public ProductoArchivoResponse(String error) {
        this.error = error;
        this.id = null;
        this.url = null;
        this.orden = 0;
    }

}
