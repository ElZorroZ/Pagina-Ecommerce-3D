package com.formaprogramada.ecommerce_backend.Domain.Service.Producto;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.*;
import com.formaprogramada.ecommerce_backend.Domain.Service.ImgBB.ImgBBUploaderService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.ImgBB.ImgBBData;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.*;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Categoria.JpaCategoriaRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.*;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.ProductoAprobado.JpaProductoAprobacionRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.ProductoAprobado.JpaProductoColorAprobacionRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.ProductoAprobado.JpaProductoDetalleAprobacionRepository;
import com.formaprogramada.ecommerce_backend.Mapper.Producto.ArchivoMapper;
import com.formaprogramada.ecommerce_backend.Mapper.Producto.ProductoDTOMapper;
import com.formaprogramada.ecommerce_backend.Mapper.Producto.ProductoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private JpaProductoRepository productoRepository;
    @Autowired
    private JpaProductoAprobacionRepository productoAprobacionRepository;
    @Autowired
    private JpaProductoColorRepository productoColorRepository;
    @Autowired
    private JpaProductoColorAprobacionRepository productoColorAprobacionRepository;
    @Autowired
    private JpaProductoArchivoRepository productoArchivoRepository;
    @Autowired
    private JpaProductoDetalleRepository productoDetalleRepository;
    @Autowired
    private JpaProductoDetalleAprobacionRepository productoDetalleAprobacionRepository;
    @Autowired
    private ProductoCacheService productoCacheService;
    @Autowired
    private JpaProductoDestacadoRepository productoDestacadoRepository;
    @Autowired
    private JpaCategoriaRepository categoriaRepository;
    private ImgBBUploaderService imgBBUploaderService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(ProductoServiceImpl.class);
    @Autowired
    private CacheManager cacheManager;

    @Caching(evict = {
            @CacheEvict(value = "productos", allEntries = true),
            @CacheEvict(value = "productosDestacados", allEntries = true)
    })
    @Transactional
    public ProductoResponse crearProducto(ProductoRequestConColores dto, MultipartFile archivoStl) throws IOException {

        // Construir código concatenado de forma segura
        String codigoInicial = dto.getCodigoInicial() != null ? dto.getCodigoInicial() : "";
        String versionStr = dto.getVersion() != null ? dto.getVersion() : "";
        String seguimiento = dto.getSeguimiento() != null ? dto.getSeguimiento() : "";
        String codigo = codigoInicial + versionStr + seguimiento;

        // Crear y guardar producto base
        ProductoEntity producto = new ProductoEntity();
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setCodigo(codigo);

        // archivoStl es MultipartFile
        if (archivoStl != null && !archivoStl.isEmpty()) {
            producto.setArchivo(archivoStl.getBytes()); // No uses decode acá
        }

        CategoriaEntity categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        producto.setCategoriaId(categoria);

        ProductoEntity productoGuardado = productoRepository.save(producto);

        // Construir string dimension (alto x ancho x profundidad)
        String dimension = dto.getDimensionAlto() + "x" + dto.getDimensionAncho() + "x" + dto.getDimensionProfundidad();

        // Guardar detalle producto
        ProductoDetalleEntity detalle = ProductoDetalleEntity.builder()
                .productoId(productoGuardado.getId())
                .dimension(dimension)
                .material(dto.getMaterial())
                .tecnica(dto.getTecnica())
                .peso(dto.getPeso() != null ? dto.getPeso() : "")
                .build();

        productoDetalleRepository.save(detalle);

        // Guardar colores
        productoColorRepository.deleteByProducto_Id(productoGuardado.getId());
        List<String> colores = dto.getColores();
        if (colores != null && !colores.isEmpty()) {
            List<ProductoColorEntity> coloresEntities = colores.stream()
                    .map(color -> new ProductoColorEntity(0, productoGuardado, color))
                    .toList();
            productoColorRepository.saveAll(coloresEntities);
        }

        // Obtener colores guardados para la respuesta
        List<String> coloresGuardados = productoColorRepository.findByProductoId(productoGuardado.getId())
                .stream()
                .map(ProductoColorEntity::getColor)
                .toList();

        ProductoCompletoDTO completo = null;
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

        return new ProductoResponse(productoGuardado, detalle, coloresGuardados);
    }




    @Caching(evict = {
            @CacheEvict(value = "producto", key = "#id"),
            @CacheEvict(value = "productoCompleto", key = "#id"),
            @CacheEvict(value = "productos", allEntries = true),
            @CacheEvict(value = "productosDestacados", allEntries = true)
    })
    @Transactional
    public ProductoEntity actualizarProductoCompleto(
            Integer id,
            ProductoCompletoDTO dto,
            List<MultipartFile> archivosNuevos, MultipartFile archivoStl) throws IOException {
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
            CategoriaEntity categoria = categoriaRepository.findById(dto.getProducto().getCategoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            producto.setCategoriaId(categoria);
        }
        // Construir código concatenado de forma segura
        String codigoInicial = dto.getProducto().getCodigoInicial() != null ? dto.getProducto().getCodigoInicial() : "";
        String versionStr = dto.getProducto().getVersion() != null ? dto.getProducto().getVersion() : "";
        String seguimiento = dto.getProducto().getSeguimiento() != null ? dto.getProducto().getSeguimiento() : "";

        String codigo = codigoInicial + versionStr + seguimiento;
        producto.setCodigo(codigo);

        // Guardar archivo STL si existe
        // Guardar archivo STL si existe y es distinto
        if (archivoStl != null && !archivoStl.isEmpty()) {
            byte[] archivoActual = producto.getArchivo();
            byte[] archivoNuevo = archivoStl.getBytes();

            if (!Arrays.equals(archivoActual, archivoNuevo)) {
                producto.setArchivo(archivoNuevo);
            }
        }


        ProductoEntity productoActualizado = productoRepository.save(producto);
        // Actualizar producto_detalle asociado
        List<ProductoDetalleEntity> detalles = productoDetalleRepository.findByProductoId(producto.getId());
        ProductoDetalleEntity detalle;
        if (detalles.isEmpty()) {
            detalle = new ProductoDetalleEntity();
            detalle.setProductoId(producto.getId());
        } else {
            detalle = detalles.get(0);
        }

        // Concatenar dimensiones
        String alto = dto.getProducto().getDimensionAlto() != null ? dto.getProducto().getDimensionAlto().trim() : "";
        String ancho = dto.getProducto().getDimensionAncho() != null ? dto.getProducto().getDimensionAncho().trim() : "";
        String profundidad = dto.getProducto().getDimensionProfundidad() != null ? dto.getProducto().getDimensionProfundidad().trim() : "";
        String dimensionConcatenada = String.join("x", alto, ancho, profundidad);

        detalle.setDimension(dimensionConcatenada);
        detalle.setMaterial(dto.getProducto().getMaterial());
        detalle.setTecnica(dto.getProducto().getTecnica());
        detalle.setPeso(dto.getProducto().getPeso());

        productoDetalleRepository.save(detalle);
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
        productoCacheService.refrescarCacheProducto(id);
        return productoActualizado;
    }

    @Cacheable(value = "producto", key = "#id")
    @Override
    public ProductoEntity obtenerProductoPorId(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }


    @Override
    @Cacheable(value = "productoCompleto", key = "#productoId", unless = "#result == null || #result.producto == null")
    @Transactional
    public ProductoCompletoDTO obtenerProductoCompleto(Integer productoId) {
        return obtenerProductoCompletoSinCache(productoId);
    }

    public ProductoCompletoDTO obtenerProductoCompletoSinCache(Integer productoId) {
        try {
            return jdbcTemplate.execute((Connection con) -> {
                CallableStatement cs = con.prepareCall("{call sp_getProductoCompleto(?)}");
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

                ProductoCompletoDTO resultado = new ProductoCompletoDTO();
                ProductoDTO prod = new ProductoDTO();

                prod.setId(rsProducto.getInt("id"));
                prod.setNombre(rsProducto.getString("nombre"));
                prod.setDescripcion(rsProducto.getString("descripcion"));
                prod.setCategoriaId(rsProducto.getInt("categoriaId"));
                prod.setPrecio(rsProducto.getFloat("precio"));
                prod.setArchivoStl(rsProducto.getString("archivo"));

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
                            archivo.setLinkArchivo(rsArchivos.getString("linkArchivo")); // Asegurate que exista en el SP
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


    @Cacheable(value = "productos")
    @Override
    public List<ProductoResponseConDestacado> listarProductos() {
        return productoRepository.findAll()
                .stream()
                .map(producto -> {
                    boolean destacado = productoDestacadoRepository.existsByProductoId(producto.getId());
                    return new ProductoResponseConDestacado(producto, destacado);
                })
                .collect(Collectors.toList());
    }
    @Cacheable("productosIds")
    public List<Integer> obtenerTodosLosIds() {
        return productoRepository.findAll().stream()
                .map(ProductoEntity::getId)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "productosDestacados")
    @Override
    public Page<ProductoConArchivoPrincipalYColoresDTO> obtenerTodosConArchivoPrincipalYColores(Pageable pageable) {
        Page<ProductoDestacadoEntity> destacadosPage = productoDestacadoRepository.findAll(pageable);

        List<ProductoConArchivoPrincipalYColoresDTO> dtoList = destacadosPage.stream().map(destacado -> {
            ProductoEntity producto = destacado.getProducto();

            ProductoResponseDTO productoResponseDTO = ProductoMapper.toDTO(producto);
            ProductoDTO productoDTO = ProductoDTOMapper.fromResponseDTO(productoResponseDTO);

            List<String> colores = productoColorRepository.findByProductoId(producto.getId()).stream()
                    .map(ProductoColorEntity::getColor)
                    .collect(Collectors.toList());

            ArchivoDTO archivoPrincipal = productoArchivoRepository.findByProductoIdOrderByOrdenAsc(producto.getId())
                    .stream()
                    .findFirst()
                    .map(ArchivoMapper::toArchivoDTO)
                    .orElse(null);

            ProductoConArchivoPrincipalYColoresDTO dto = new ProductoConArchivoPrincipalYColoresDTO();
            dto.setProducto(productoDTO);
            dto.setColores(colores);
            dto.setArchivoPrincipal(archivoPrincipal);

            return dto;
        }).collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, destacadosPage.getTotalElements());
    }


    @Caching(evict = {
            @CacheEvict(value = "producto", key = "#id"),
            @CacheEvict(value = "productoCompleto", key = "#id"),
            @CacheEvict(value = "productos", allEntries = true),
            @CacheEvict(value = "productosDestacados", allEntries = true)
    })
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
        productoCacheService.refrescarCacheProducto(id);
    }
    @Cacheable(value = "productoCompleto", key = "#categoriaId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<ProductoResponseDTO> listarProductosPorCategoria(Integer categoriaId, Pageable pageable) {
        Page<ProductoEntity> productosPage = productoRepository.findByCategoriaId_Id(categoriaId, pageable);
        return productosPage.map(ProductoMapper::toDTO);
    }


    @Override
    @Cacheable(value = "productosTodos")
    public Page<ProductoConArchivoPrincipalYColoresDTO> obtenerTodosLosProductosConColoresYArchivo(Pageable pageable) {
        Page<ProductoEntity> productosPage = productoRepository.findAll(pageable);

        return productosPage.map(producto -> {
            // Convertir a ProductoDTO
            ProductoResponseDTO responseDTO = ProductoMapper.toDTO(producto);
            ProductoDTO productoDTO = ProductoDTOMapper.fromResponseDTO(responseDTO);

            // Obtener colores
            List<String> colores = productoColorRepository.findByProductoId(producto.getId())
                    .stream()
                    .map(ProductoColorEntity::getColor)
                    .collect(Collectors.toList());

            // Obtener archivo principal (orden 1 o el primero)
            ArchivoDTO archivoPrincipal = productoArchivoRepository.findByProductoIdOrderByOrdenAsc(producto.getId())
                    .stream()
                    .findFirst()
                    .map(ArchivoMapper::toArchivoDTO)
                    .orElse(null);

            // Armar el DTO final
            ProductoConArchivoPrincipalYColoresDTO dto = new ProductoConArchivoPrincipalYColoresDTO();
            dto.setProducto(productoDTO);
            dto.setColores(colores);
            dto.setArchivoPrincipal(archivoPrincipal);

            return dto;
        });
    }






}

