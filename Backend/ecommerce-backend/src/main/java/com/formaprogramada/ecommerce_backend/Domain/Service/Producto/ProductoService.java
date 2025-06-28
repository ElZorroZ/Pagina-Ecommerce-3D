package com.formaprogramada.ecommerce_backend.Domain.Service.Producto;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoCompletoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoRequestConColores;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoResponse;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductoService {
    ProductoEntity crearProducto(ProductoRequestConColores dto, List<String> colores);
    ProductoEntity obtenerProductoPorId(Integer id);
    List<ProductoResponse> listarProductos();
    ProductoEntity actualizarProductoCompleto(
            Integer id,
            ProductoCompletoDTO dto,
            List<MultipartFile> archivosNuevos
    ) throws IOException;
    void eliminarProducto(Integer id);
    ProductoCompletoDTO obtenerProductoCompleto(Integer id);

}

