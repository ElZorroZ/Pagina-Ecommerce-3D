package com.formaprogramada.ecommerce_backend.Domain.Service.Producto;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.*;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductoService {
    ProductoResponse crearProducto(ProductoRequestConColores dto, MultipartFile archivoStl) throws IOException;
    ProductoEntity obtenerProductoPorId(Integer id);

    List<ProductoResponseConDestacado> listarProductos();
    ProductoEntity actualizarProductoCompleto(
            Integer id,
            ProductoCompletoDTO dto,
            List<MultipartFile> archivosNuevos, MultipartFile archivoStl
    ) throws IOException;
    void eliminarProducto(Integer id);
    List<Integer> obtenerTodosLosIds();
    ProductoCompletoDTO obtenerProductoCompletoSinCache(Integer productoId);
    ProductoCompletoDTO obtenerProductoCompleto(Integer id);
    List<ProductoConArchivoPrincipalYColoresDTO> obtenerProductosDestacados();
    Page<ProductoResponseDTO> listarProductosPorCategoria(Integer categoriaId, Pageable pageable);
    Page<ProductoConArchivoPrincipalYColoresDTO> obtenerTodosLosProductosConColoresYArchivo(Pageable pageable);
    List<ProductoConArchivoPrincipalYColoresDTO> obtenerTodosLosProductosSinPaginado();
    List<ProductoResponseDTO> listarProductosPorCategoriaSP(Integer categoriaId, Pageable pageable);
    long contarProductosPorCategoria(Integer categoriaId);
    ProductoConArchivoPrincipalYColoresDTO obtenerUltimoProducto();



    }

