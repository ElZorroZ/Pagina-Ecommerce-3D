package com.formaprogramada.ecommerce_backend.Domain.Service.Producto;

import com.formaprogramada.ecommerce_backend.Domain.Service.ImgBB.ImgBBUploaderService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.*;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoAprobacionEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoColorAprobacionEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoDetalleAprobacionEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Categoria.JpaCategoriaRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.*;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.ProductoAprobado.JpaProductoAprobacionRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.ProductoAprobado.JpaProductoArchivoAprobadoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.ProductoAprobado.JpaProductoColorAprobacionRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.ProductoAprobado.JpaProductoDetalleAprobacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
@Service
public class ProductoAprobadoServiceImpl implements ProductoAprobadoService{
    @Autowired
    private JpaProductoAprobacionRepository productoAprobacionRepository;
    @Autowired
    private JpaProductoColorAprobacionRepository productoColorAprobacionRepository;
    @Autowired
    private JpaProductoArchivoAprobadoRepository productoArchivoRepository;
    @Autowired
    private JpaProductoDetalleAprobacionRepository productoDetalleAprobacionRepository;
    @Autowired
    private ProductoAprobadoCacheService productoCacheService;
    @Autowired
    private JpaCategoriaRepository categoriaRepository;
    private ImgBBUploaderService imgBBUploaderService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(ProductoServiceImpl.class);
    @Autowired
    private CacheManager cacheManager;

    @Override
    @Transactional
    public ProductoAprobacionResponse crearAprobacionProducto(ProductoAprobacionRequest dto, MultipartFile archivoStl) throws IOException {
        // Construir código concatenado de forma segura
        String codigoInicial = dto.getCodigoInicial() != null ? dto.getCodigoInicial() : "";
        String versionStr = dto.getVersion() != null ? dto.getVersion() : "";
        String seguimiento = dto.getSeguimiento() != null ? dto.getSeguimiento() : "";
        String codigo = codigoInicial + versionStr + seguimiento;


        // Crear y guardar producto base
        ProductoAprobacionEntity producto = new ProductoAprobacionEntity();
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setCodigo(codigo);
        producto.setUsuarioId(dto.getCreadorId());

        // archivoStl es MultipartFile
        if (archivoStl != null && !archivoStl.isEmpty()) {
            producto.setArchivo(archivoStl.getBytes()); // No uses decode acá
        }

        CategoriaEntity categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        producto.setCategoriaId(categoria);

        ProductoAprobacionEntity productoGuardado = productoAprobacionRepository.save(producto);

        // Construir string dimension (alto x ancho x profundidad)
        String dimension = dto.getDimensionAlto() + "x" + dto.getDimensionAncho() + "x" + dto.getDimensionProfundidad();

        // Guardar detalle producto
        ProductoDetalleAprobacionEntity detalle = ProductoDetalleAprobacionEntity.builder()
                .productoId(productoGuardado.getId())
                .dimension(dimension)
                .material(dto.getMaterial())
                .tecnica(dto.getTecnica())
                .peso(dto.getPeso() != null ? dto.getPeso() : "")
                .build();

        productoDetalleAprobacionRepository.save(detalle);

        // Guardar colores
        productoColorAprobacionRepository.deleteByProducto_Id(productoGuardado.getId());
        List<String> colores = dto.getColores();
        if (colores != null && !colores.isEmpty()) {
            List<ProductoColorAprobacionEntity> coloresEntities = colores.stream()
                    .map(color -> new ProductoColorAprobacionEntity(0, productoGuardado, color))
                    .toList();
            productoColorAprobacionRepository.saveAll(coloresEntities);
        }

        // Obtener colores guardados para la respuesta
        List<String> coloresGuardados = productoColorAprobacionRepository.findByProductoId(productoGuardado.getId())
                .stream()
                .map(ProductoColorAprobacionEntity::getColor)
                .toList();



        ProductoCompletoAprobacionDTO completo = null;
        for (int i = 0; i < 3; i++) {
            completo = obtenerProductoCompletoSinCache(productoGuardado.getId());
            if (completo != null && completo.getProducto() != null) break;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Buena práctica: volver a marcar el hilo como interrumpido
                throw new RuntimeException("Interrumpido al esperar para volver a obtener el producto completo", e);
            }
        }
        if (completo != null && completo.getProducto() != null) {
            cacheManager.getCache("productoCompleto").put(productoGuardado.getId(), completo);
        }
        productoCacheService.refrescarCacheProducto(productoGuardado.getId());
        return new ProductoAprobacionResponse(productoGuardado, detalle, coloresGuardados);
    }

    @Override
    public ProductoCompletoAprobacionDTO obtenerProductoCompleto(Integer id) {
        return obtenerProductoCompletoSinCache(id);
    }

    @Override
    public void aprobarProducto(Integer id) {
        
    }


    public ProductoCompletoAprobacionDTO obtenerProductoCompletoSinCache(Integer productoId) {
        try {
            return jdbcTemplate.execute((Connection con) -> {
                CallableStatement cs = con.prepareCall("{call sp_getProductoAprobacionCompleto(?)}");
                cs.setInt(1, productoId);
                boolean hasResults = cs.execute();

                if (!hasResults) {
                    System.err.println("No se encontraron resultados para el producto con ID: " + productoId);
                    return null;
                }

                ResultSet rsProducto = cs.getResultSet();
                if (!rsProducto.next()) {
                    System.err.println("Producto no encontrado para ID: " + productoId);
                    return null;
                }

                ProductoCompletoAprobacionDTO resultado = new ProductoCompletoAprobacionDTO();
                ProductoAprobacioDTO prod = new ProductoAprobacioDTO();

                prod.setId(rsProducto.getInt("id"));
                prod.setNombre(rsProducto.getString("nombre"));
                prod.setDescripcion(rsProducto.getString("descripcion"));
                prod.setCategoriaId(rsProducto.getInt("categoriaId"));
                prod.setPrecio(rsProducto.getFloat("precio"));
                prod.setArchivoStl(rsProducto.getString("archivo"));
                prod.setUsuarioId(rsProducto.getInt("idUsuarioCreador"));

                String codigo = rsProducto.getString("codigo");
                if (codigo != null && codigo.length() >= 7) {
                    prod.setCodigoInicial(codigo.substring(0, 3));

                    String versionString = codigo.substring(3, 7);
                    if (versionString.matches("\\d+")) {
                        prod.setVersion(versionString);
                    } else {
                        System.err.println("Versión inválida: " + versionString);
                        prod.setVersion("0");
                    }

                    if (codigo.length() > 7) {
                        prod.setSeguimiento(codigo.substring(7));
                    }
                }

                String dim = rsProducto.getString("dimension");
                if (dim != null) {
                    String[] partes = dim.split("x");
                    if (partes.length == 3) {
                        prod.setDimensionAlto(partes[0]);
                        prod.setDimensionAncho(partes[1]);
                        prod.setDimensionProfundidad(partes[2]);
                    }
                }

                prod.setMaterial(rsProducto.getString("material"));
                prod.setTecnica(rsProducto.getString("tecnica"));
                prod.setPeso(rsProducto.getString("peso"));

                resultado.setProducto(prod);

                // Colores
                if (cs.getMoreResults()) {
                    ResultSet rsColores = cs.getResultSet();
                    List<String> colores = new ArrayList<>();
                    while (rsColores.next()) {
                        colores.add(rsColores.getString("Color"));
                    }
                    resultado.setColores(colores);
                }

                // Archivos
                if (cs.getMoreResults()) {
                    try {
                        ResultSet rsArchivos = cs.getResultSet();
                        List<ArchivoDTO> archivos = new ArrayList<>();
                        while (rsArchivos.next()) {
                            ArchivoDTO archivo = new ArchivoDTO();
                            archivo.setId(rsArchivos.getInt("id"));
                            archivo.setProductId(rsArchivos.getInt("productId"));
                            archivo.setLinkArchivo(rsArchivos.getString("archivoImagen")); // Asegurate que exista en el SP
                            archivo.setOrden(rsArchivos.getInt("orden"));
                            archivos.add(archivo);
                        }
                        resultado.setArchivos(archivos);
                    } catch (SQLException e) {
                        System.err.println("Error procesando archivos: " + e.getMessage());
                    }
                } else {
                    System.out.println("No hay resultset de archivos");
                }

                // Validación antes de devolver
                if (resultado.getProducto() == null || resultado.getProducto().getId() == null) {
                    System.err.println("Producto incompleto → no se cachea");
                    return null;
                }

                return resultado;
            });
        } catch (Exception e) {
            System.err.println("Error en obtenerProductoCompleto: " + productoId + " → " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
