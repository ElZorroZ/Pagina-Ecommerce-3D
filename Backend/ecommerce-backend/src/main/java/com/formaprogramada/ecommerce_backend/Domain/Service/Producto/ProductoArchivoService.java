package com.formaprogramada.ecommerce_backend.Domain.Service.Producto;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoArchivoEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductoArchivoService {
    ProductoArchivoEntity agregarArchivo(ProductoArchivoEntity archivo);
    ProductoArchivoEntity agregarArchivoConImagen(Integer productoId, MultipartFile file, Integer orden) throws IOException;
    List<ProductoArchivoEntity> obtenerArchivosPorProductoId(Integer productoId);
    void eliminarArchivo(Integer id);
}

