package com.formaprogramada.ecommerce_backend.Domain.Service.Producto;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ProductoAprobacionRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ProductoAprobacionResponse;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ProductoCompletoAprobacionDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoAprobacionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;


public interface ProductoAprobadoService {

    ProductoAprobacionResponse crearAprobacionProducto(ProductoAprobacionRequest dto, MultipartFile archivoStl) throws IOException;
    ProductoCompletoAprobacionDTO obtenerProductoCompleto(Integer id);
    Boolean aprobarProducto(Integer id,String codigoInicial,String versionStr,String seguimiento);
    void borrarProducto(Integer id);
    List<ProductoCompletoAprobacionDTO> verProductosaAprobar();
    List<ProductoCompletoAprobacionDTO> verProductosaAprobarDeX(int id);
    List<ProductoCompletoAprobacionDTO> verProductoCompleto(int id);

}

