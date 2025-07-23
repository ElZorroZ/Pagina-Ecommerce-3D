package com.formaprogramada.ecommerce_backend.Domain.Service.Producto;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;


public interface ProductoAprobadoService {

    ProductoAprobacionResponse crearAprobacionProducto(ProductoAprobacionRequest dto, MultipartFile archivoStl) throws IOException;
    ProductoCompletoAprobacionDTO obtenerProductoCompleto(Integer id);
    void aprobarProducto(Integer id);

}

