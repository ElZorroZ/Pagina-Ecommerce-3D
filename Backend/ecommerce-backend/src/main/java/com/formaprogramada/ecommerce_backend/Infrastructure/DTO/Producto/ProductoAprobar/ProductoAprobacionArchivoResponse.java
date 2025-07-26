package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoArchivoAprobacionEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoArchivoEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductoAprobacionArchivoResponse {
    private Integer id;
    private byte[] archivoImagen;
    private int orden;
    private String error;


    public ProductoAprobacionArchivoResponse(ProductoArchivoAprobacionEntity entity) {
        this.id = entity.getId();
        this.archivoImagen = entity.getArchivoImagen();  // acá el nombre correcto
        this.orden = entity.getOrden();
        this.error = null;  // porque acá no hay error
    }
    public ProductoAprobacionArchivoResponse(String error) {
        this.error = error;
        this.id = null;
        this.archivoImagen = null;
        this.orden = 0;
    }
}
