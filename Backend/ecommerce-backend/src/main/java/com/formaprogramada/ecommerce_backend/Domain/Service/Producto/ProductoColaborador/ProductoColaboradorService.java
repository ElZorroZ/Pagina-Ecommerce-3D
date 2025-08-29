package com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoColaborador;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ProductoAprobacionRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ProductoAprobacionResponse;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ProductoCompletoAprobacionDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoAprobacionEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductoColaboradorService {
    ProductoAprobacionResponse crearAprobacionProducto(ProductoAprobacionRequest dto, MultipartFile archivoStl) throws IOException;
    ProductoCompletoAprobacionDTO obtenerProductoCompleto(Integer id);
    void borrarProducto(Integer id);
    List<ProductoCompletoAprobacionDTO> verProductosaAprobarDeX(int id);
    ProductoAprobacionEntity actualizarProductoCompleto(int id, ProductoCompletoAprobacionDTO productoCompletoDTO, List<MultipartFile> archivosNuevos, MultipartFile archivoComprimido,         String eliminarArchivoComprimido);
    ProductoCompletoAprobacionDTO obtenerProductoCompletoSinCache(Integer id); // agregar aquí
    List<ProductoCompletoAprobacionDTO> verProductosaAprobar();
    Boolean aprobarProducto(Integer id, String codigoInicial, String versionStr, String seguimiento);
}
