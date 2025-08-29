package com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoColaborador;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoArchivoAprobacionEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoArchivoEntity;
import org.springframework.web.multipart.MultipartFile;

public interface ProductoArchivoColaboradorService {
    ProductoArchivoAprobacionEntity agregarArchivo(Integer productoId, MultipartFile file, Integer orden);
}
