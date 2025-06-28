package com.formaprogramada.ecommerce_backend.Domain.Service.Producto;

import com.formaprogramada.ecommerce_backend.Domain.Service.ImgBB.ImgBBUploaderService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.ImgBB.ImgBBData;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.*;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoArchivoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoColorEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.JpaProductoArchivoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.JpaProductoColorRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.JpaProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private JpaProductoRepository productoRepository;
    @Autowired
    private JpaProductoColorRepository productoColorRepository;
    @Autowired
    private JpaProductoArchivoRepository productoArchivoRepository;
    @Autowired
    private ImgBBUploaderService imgBBUploaderService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(ProductoServiceImpl.class);

    @Transactional
    public ProductoEntity crearProducto(ProductoRequestConColores dto, List<String> colores) {
        ProductoEntity producto = new ProductoEntity();
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());

        CategoriaEntity categoria = new CategoriaEntity();
        categoria.setId(dto.getCategoriaId() != null ? dto.getCategoriaId() : 1);
        producto.setCategoriaId(categoria);

        ProductoEntity productoGuardado = productoRepository.save(producto);

        // Borrar colores anteriores (si corresponde)
        productoColorRepository.deleteByProducto_Id(productoGuardado.getId());

        // Guardar nuevos colores
        if (colores != null && !colores.isEmpty()) {
            List<ProductoColorEntity> coloresEntities = colores.stream()
                    .map(color -> new ProductoColorEntity(0, productoGuardado, color))
                    .toList();
            System.out.println("Guardando colores: " + colores);
            productoColorRepository.saveAll(coloresEntities);
        }
        return productoGuardado;
    }

    @Transactional
    public ProductoEntity actualizarProductoCompleto(
            Integer id,
            ProductoCompletoDTO dto,
            List<MultipartFile> archivosNuevos) throws IOException {
        System.out.println("DTO Archivos:");
        dto.getArchivos().forEach(a -> System.out.println(a.getLinkArchivo()));
        System.out.println("DTO Colores:");
        dto.getColores().forEach(System.out::println);

        // 1. Buscar producto existente
        ProductoEntity producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // 2. Actualizar campos básicos (puede ser con SP o normal)
        producto.setNombre(dto.getProducto().getNombre());
        producto.setDescripcion(dto.getProducto().getDescripcion());
        producto.setPrecio(dto.getProducto().getPrecio());

        if (dto.getProducto().getCategoriaId() != null) {
            CategoriaEntity categoria = new CategoriaEntity();
            categoria.setId(dto.getProducto().getCategoriaId());
            producto.setCategoriaId(categoria);
        }

        ProductoEntity productoActualizado = productoRepository.save(producto);

        // 3. Borrar colores anteriores
        productoColorRepository.deleteByProducto_Id(productoActualizado.getId());

        // 4. Guardar colores nuevos
        List<String> colores = dto.getColores();
        if (colores != null && !colores.isEmpty()) {
            List<ProductoColorEntity> coloresEntities = colores.stream()
                    .map(color -> new ProductoColorEntity(0, productoActualizado, color))
                    .toList();
            productoColorRepository.saveAll(coloresEntities);
        }

        // 5. Borrar imágenes antiguas (en BDD y en ImgBB)
        List<ProductoArchivoEntity> archivosExistentes = productoArchivoRepository.findByProductoIdOrderByOrdenAsc(productoActualizado.getId());

        Set<String> urlsQuePermanecen = dto.getArchivos().stream()
                .map(ArchivoDTO::getLinkArchivo)
                .collect(Collectors.toSet());

        for (ProductoArchivoEntity archivo : archivosExistentes) {
            if (!urlsQuePermanecen.contains(archivo.getLinkArchivo())) {
                // Borrar imagen de ImgBB
                imgBBUploaderService.borrarImagenDeImgBB(archivo.getDeleteUrl());
                // Borrar de BDD
                productoArchivoRepository.delete(archivo);
            }
        }

        // 6. Subir nuevas imágenes y guardar URLs en BDD
        if (archivosNuevos != null && !archivosNuevos.isEmpty()) {
            int orden = 0;
            for (MultipartFile archivoNuevo : archivosNuevos) {
                ImgBBData data = imgBBUploaderService.subirImagen(archivoNuevo);

                ProductoArchivoEntity nuevoArchivo = new ProductoArchivoEntity();
                nuevoArchivo.setProducto(productoActualizado);
                nuevoArchivo.setLinkArchivo(data.getUrl());
                nuevoArchivo.setDeleteUrl(data.getDelete_url());
                nuevoArchivo.setOrden(orden++);
                productoArchivoRepository.save(nuevoArchivo);
            }

        }

        return productoActualizado;
    }
    @Override
    public ProductoEntity obtenerProductoPorId(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    public ProductoCompletoDTO obtenerProductoCompleto(Integer productoId) {
        return jdbcTemplate.execute((Connection con) -> {
            CallableStatement cs = con.prepareCall("{call sp_getProductoCompleto(?)}");
            cs.setInt(1, productoId);
            boolean hasResults = cs.execute();

            ProductoCompletoDTO resultado = new ProductoCompletoDTO();

            if (hasResults) {
                ResultSet rsProducto = cs.getResultSet();
                if (rsProducto.next()) {
                    ProductoDTO prod = new ProductoDTO();
                    prod.setId(rsProducto.getInt("id"));
                    prod.setNombre(rsProducto.getString("nombre"));
                    prod.setDescripcion(rsProducto.getString("descripcion"));
                    prod.setCategoriaId(rsProducto.getInt("categoriaId"));
                    prod.setPrecio(rsProducto.getFloat("precio"));
                    prod.setArchivo(rsProducto.getString("archivo"));
                    resultado.setProducto(prod);
                }

                // Pasar al siguiente result set para colores
                if (cs.getMoreResults()) {
                    ResultSet rsColores = cs.getResultSet();
                    List<String> colores = new ArrayList<>();
                    while (rsColores.next()) {
                        colores.add(rsColores.getString("Color"));
                    }
                    resultado.setColores(colores);
                }

                // Pasar al siguiente result set para archivos
                if (cs.getMoreResults()) {
                    ResultSet rsArchivos = cs.getResultSet();
                    List<ArchivoDTO> archivos = new ArrayList<>();
                    while (rsArchivos.next()) {
                        ArchivoDTO archivo = new ArchivoDTO();
                        archivo.setId(rsArchivos.getInt("id"));
                        archivo.setProductId(rsArchivos.getInt("productId"));
                        archivo.setLinkArchivo(rsArchivos.getString("linkArchivo"));
                        archivo.setOrden(rsArchivos.getInt("orden"));
                        archivos.add(archivo);
                    }
                    resultado.setArchivos(archivos);
                }
            }
            return resultado;
        });
    }

    @Override
    public List<ProductoResponse> listarProductos() {
        return productoRepository.findAll()
                .stream()
                .map(ProductoResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminarProducto(Integer id) {
        // 1. Obtener las imágenes relacionadas
        List<ProductoArchivoEntity> archivos = productoArchivoRepository.findByProductoIdOrderByOrdenAsc(id);

        // 2. Borrar imágenes en imgBB
        for (ProductoArchivoEntity archivo : archivos) {
            String imgBBId = archivo.getDeleteUrl(); // supongamos que guardás ese ID para borrar en imgBB
            if (imgBBId != null) {
                try {
                    imgBBUploaderService.borrarImagenDeImgBB(imgBBId); // llamás a un servicio que haga el DELETE en imgBB
                } catch (Exception e) {
                    // loguear error pero seguir para no impedir la eliminación local
                    logger.error("Error eliminando imagen en imgBB: " + imgBBId, e);
                }
            }
        }

        // 3. Eliminar relaciones (colores, archivos, etc.) y producto localmente
        productoRepository.deleteById(id);
    }
}

