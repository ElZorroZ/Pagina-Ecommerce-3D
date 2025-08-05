package com.formaprogramada.ecommerce_backend.Domain.Service.Producto;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ProductoAprobadoConArchivoPrincipalYColoresDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.*;
import com.formaprogramada.ecommerce_backend.Domain.Service.ImgBB.ImgBBUploaderService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.ImgBB.ImgBBData;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.*;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Categoria.JpaCategoriaRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.*;
import com.formaprogramada.ecommerce_backend.Mapper.Producto.ArchivoMapper;
import com.formaprogramada.ecommerce_backend.Mapper.Producto.ProductoDTOMapper;
import com.formaprogramada.ecommerce_backend.Mapper.Producto.ProductoMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
import java.util.concurrent.ConcurrentMap;
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
    private JpaProductoDetalleRepository productoDetalleRepository;
    @Autowired
    private ProductoCacheService productoCacheService;
    @Autowired
    @Lazy
    private ProductoCacheProxyService productoCacheProxyService;
    @Autowired
    private JpaProductoDestacadoRepository productoDestacadoRepository;
    @Autowired
    private JpaCategoriaRepository categoriaRepository;
    @Autowired
    private ImgBBUploaderService imgBBUploaderService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(ProductoServiceImpl.class);
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private ProductosPorCategoriaCacheKeys productosPorCategoriaCacheKeys;

    @Transactional
    public ProductoResponse crearProducto(ProductoRequestConColores dto, MultipartFile archivoComprimido) throws IOException {
        try {
            if (productoRepository.existsByNombre(dto.getNombre())) {
                throw new IllegalArgumentException("Ya existe un producto con ese nombre");
            }

            System.out.println("⚠️ Creando producto");
            String codigoInicial = dto.getCodigoInicial() != null ? dto.getCodigoInicial() : "";
            String versionStr = dto.getVersion() != null ? dto.getVersion() : "";
            String seguimiento = dto.getSeguimiento() != null ? dto.getSeguimiento() : "";
            String codigo = codigoInicial + versionStr + seguimiento;

            ProductoEntity producto = new ProductoEntity();
            producto.setNombre(dto.getNombre());
            producto.setDescripcion(dto.getDescripcion());
            producto.setPrecio(dto.getPrecio());
            producto.setPrecioDigital(dto.getPrecioDigital());
            producto.setCodigo(codigo);

            if (archivoComprimido != null && !archivoComprimido.isEmpty()) {
                producto.setArchivo(archivoComprimido.getBytes());
            }

            CategoriaEntity categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            producto.setCategoriaId(categoria);

            ProductoEntity productoGuardado = productoRepository.save(producto);

            String dimension = dto.getDimensionAlto() + "x" + dto.getDimensionAncho() + "x" + dto.getDimensionProfundidad();

            ProductoDetalleEntity detalle = ProductoDetalleEntity.builder()
                    .productoId(productoGuardado.getId())
                    .dimension(dimension)
                    .material(dto.getMaterial())
                    .tecnica(dto.getTecnica())
                    .peso(dto.getPeso() != null ? dto.getPeso() : "")
                    .build();

            productoDetalleRepository.save(detalle);

            productoColorRepository.deleteByProducto_Id(productoGuardado.getId());
            List<String> colores = dto.getColores();
            if (colores != null && !colores.isEmpty()) {
                List<ProductoColorEntity> coloresEntities = colores.stream()
                        .map(color -> new ProductoColorEntity(0, productoGuardado, color))
                        .toList();
                productoColorRepository.saveAll(coloresEntities);
            }

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
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrumpido al esperar para obtener producto completo", e);
                }
            }
            if (completo != null && completo.getProducto() != null) {
                cacheManager.getCache("productoCompleto").put(productoGuardado.getId(), completo);
            }

            Cache cache = cacheManager.getCache("productosTodos");
            if (cache != null) {
                List<ProductoConArchivoPrincipalYColoresDTO> lista = cache.get("todos", List.class);
                if (lista == null) lista = new ArrayList<>();

                ProductoResponseDTO responseDTO = ProductoMapper.toDTO(productoGuardado);
                ProductoDTO productoDTO = ProductoDTOMapper.fromResponseDTO(responseDTO);
                ArchivoDTO archivoPrincipal = responseDTO.getArchivos() != null && !responseDTO.getArchivos().isEmpty()
                        ? responseDTO.getArchivos().get(0)
                        : null;
                List<String> coloresActualizados = responseDTO.getColores();

                ProductoConArchivoPrincipalYColoresDTO nuevoProductoCache = new ProductoConArchivoPrincipalYColoresDTO(
                        productoDTO, archivoPrincipal, coloresActualizados
                );

                lista.add(nuevoProductoCache);
                cache.put("todos", lista);
            }

            Cache cachePorCategoria = cacheManager.getCache("productosPorCategoria");
            if (cachePorCategoria != null && productoGuardado.getCategoriaId() != null) {
                Integer categoriaId = productoGuardado.getCategoriaId().getId();
                for (int pageNumber = 0; pageNumber <= 5; pageNumber++) {
                    String key = categoriaId + "-" + pageNumber + "-20";
                    Page<ProductoResponseDTO> pagina = cachePorCategoria.get(key, Page.class);
                    if (pagina != null) {
                        List<ProductoResponseDTO> nuevosProductos = new ArrayList<>(pagina.getContent());
                        ProductoResponseDTO nuevoDTO = ProductoMapper.toDTO(productoGuardado);
                        nuevosProductos.add(nuevoDTO);

                        nuevosProductos.sort(Comparator.comparing(ProductoResponseDTO::getId).reversed());
                        if (nuevosProductos.size() > pagina.getSize()) {
                            nuevosProductos = nuevosProductos.subList(0, pagina.getSize());
                        }

                        Page<ProductoResponseDTO> nuevaPagina = new PageImpl<>(
                                nuevosProductos, pagina.getPageable(), pagina.getTotalElements() + 1
                        );

                        cachePorCategoria.put(key, nuevaPagina);
                    }
                }
            }

            Cache cacheProductos = cacheManager.getCache("productos");
            if (cacheProductos != null) {
                List<ProductoResponseConDestacado> productos = cacheProductos.get("productos", List.class);
                if (productos == null) productos = new ArrayList<>();

                boolean esDestacado = productoDestacadoRepository.existsByProductoId(productoGuardado.getId());
                ProductoResponseConDestacado nuevo = new ProductoResponseConDestacado(productoGuardado, esDestacado);
                productos.add(nuevo);
                cacheProductos.put("productos", productos);
            }

            productoCacheService.refrescarCacheProducto(productoGuardado.getId());
            productoCacheService.precargarUltimoProducto();

            return new ProductoResponse(productoGuardado, detalle, coloresGuardados);
        } catch (Exception e) {
            throw new RuntimeException("Error al crear el producto: " + e.getMessage(), e);
        }
    }

    @Caching(evict = {
            @CacheEvict(value = "producto", key = "#id"),
            @CacheEvict(value = "productoCompleto", key = "#id"),
    })
    @Transactional
    public ProductoEntity actualizarProductoCompleto(
            Integer id,
            ProductoCompletoDTO dto,
            List<MultipartFile> archivosNuevos, MultipartFile archivoComprimido) throws IOException {
        System.out.println("DTO Archivos:");
        dto.getArchivos().forEach(a -> System.out.println(a.getLinkArchivo()));
        System.out.println("DTO Colores:");
        dto.getColores().forEach(System.out::println);
        try {
            System.out.println("⚠️ Actualizando producto");
            // 1. Buscar producto existente
            ProductoEntity producto = productoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            Optional<ProductoEntity> productoConMismoNombre = productoRepository.findByNombre(dto.getProducto().getNombre());
            if (productoConMismoNombre.isPresent() && !productoConMismoNombre.get().getId().equals(producto.getId())) {
                throw new RuntimeException("Ya existe un producto con ese nombre.");
            }
            // 2. Actualizar campos básicos (puede ser con SP o normal)
            producto.setNombre(dto.getProducto().getNombre());
            producto.setDescripcion(dto.getProducto().getDescripcion());
            producto.setPrecio(dto.getProducto().getPrecio());
            producto.setPrecioDigital(dto.getProducto().getPrecioDigital());
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
            if (archivoComprimido != null && !archivoComprimido.isEmpty()) {
                byte[] archivoActual = producto.getArchivo();
                byte[] archivoNuevo = archivoComprimido.getBytes();

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
            productoColorRepository.flush(); // asegura que se ejecute el delete en DB
            productoActualizado.getColores().clear(); // opcional si tenés JPA bidireccional
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
            // Convertir productoActualizado a DTO para cache
            ProductoResponseDTO responseDTO = ProductoMapper.toDTO(productoActualizado);
            ProductoDTO productoDTO = ProductoDTOMapper.fromResponseDTO(responseDTO);

            ArchivoDTO archivoPrincipal = null;
            if (responseDTO.getArchivos() != null && !responseDTO.getArchivos().isEmpty()) {
                archivoPrincipal = responseDTO.getArchivos().get(0);
            }

            List<String> coloresActualizados = responseDTO.getColores();

            ProductoConArchivoPrincipalYColoresDTO productoActualizadoDTO = new ProductoConArchivoPrincipalYColoresDTO(
                    productoDTO,
                    archivoPrincipal,
                    coloresActualizados
            );

            productoCacheService.refrescarCacheProducto(id);
            productoCacheService.refrescarTodosLosProductos(); // recalcula productosTodos
            productoCacheProxyService.precargarProductosDestacados();// recalcula destacados
            Integer categoriaId = productoActualizado.getCategoriaId().getId();
            productoCacheProxyService.precargarPorCategoria(categoriaId, PageRequest.of(0, 20));
            Cache cache = cacheManager.getCache("productosTodos");
            if (cache != null) {
                List<ProductoConArchivoPrincipalYColoresDTO> lista = cache.get("todos", List.class);
                if (lista != null) {
                    lista = lista.stream()
                            .map(p -> p.getProducto().getId().equals(productoActualizado.getId()) ? productoActualizadoDTO : p)
                            .collect(Collectors.toList());
                    cache.put("todos", lista);
                }
            }
            // Actualizar en productosDestacados si corresponde
            Cache cacheDestacados = cacheManager.getCache("productosDestacados");
            if (cacheDestacados != null) {
                List<ProductoConArchivoPrincipalYColoresDTO> destacados = cacheDestacados.get("destacados", List.class);
                if (destacados != null) {
                    boolean estabaDestacado = destacados.stream()
                            .anyMatch(p -> p.getProducto().getId().equals(productoActualizado.getId()));

                    if (estabaDestacado) {
                        List<ProductoConArchivoPrincipalYColoresDTO> actualizados = destacados.stream()
                                .map(p -> p.getProducto().getId().equals(productoActualizado.getId()) ? productoActualizadoDTO : p)
                                .collect(Collectors.toList());

                        cacheDestacados.put("destacados", actualizados);
                    }
                }
            }
            //Limpiar solo la categoría modificada en el caché productosPorCategoria
            // Actualizar en productosPorCategoria
            Cache cachePorCategoria = cacheManager.getCache("productosPorCategoria");
            if (cachePorCategoria != null && categoriaId != null) {
                String categoriaPrefix = categoriaId + "-";
                ProductoResponseDTO productoResponseParaCategoria = ProductoMapper.toDTO(productoActualizado);

                for (String key : productosPorCategoriaCacheKeys.getKeys()) {
                    if (key.startsWith(categoriaPrefix)) {
                        Cache.ValueWrapper wrapper = cachePorCategoria.get(key);
                        if (wrapper == null) continue;

                        Page<?> paginaRaw = (Page<?>) wrapper.get();
                        if (paginaRaw == null) continue;

                        List<ProductoResponseDTO> listaModificada = paginaRaw.getContent().stream()
                                .map(obj -> {
                                    ProductoResponseDTO p = (ProductoResponseDTO) obj;
                                    return p.getId().equals(productoActualizado.getId()) ? productoResponseParaCategoria : p;
                                })
                                .collect(Collectors.toList());

                        Page<ProductoResponseDTO> paginaActualizada = new PageImpl<>(
                                listaModificada,
                                paginaRaw.getPageable(),
                                paginaRaw.getTotalElements()
                        );

                        cachePorCategoria.put(key, paginaActualizada);
                    }
                }
            }
            // 🔄 Actualizar producto en cache "productos"
            Cache cacheProductos = cacheManager.getCache("productos");
            if (cacheProductos != null) {
                List<ProductoResponseConDestacado> productos = cacheProductos.get("productos", List.class);
                if (productos != null) {
                    boolean esDestacado = productoDestacadoRepository.existsByProductoId(productoActualizado.getId());
                    ProductoResponseConDestacado actualizado = new ProductoResponseConDestacado(productoActualizado, esDestacado);

                    List<ProductoResponseConDestacado> actualizados = productos.stream()
                            .map(p -> p.getId().equals(productoActualizado.getId())
                                    ? actualizado : p)
                            .collect(Collectors.toList());

                    cacheProductos.put("productos", actualizados);
                }
            }
            productoCacheService.precargarUltimoProducto();

            return productoActualizado;
        } catch (Exception e) {
            logger.error("Error actualizando producto", e);
            throw e; // para que Spring maneje la excepción y loguee
        }
    }

    @Cacheable(value = "producto", key = "#id")
    @Override
    public ProductoEntity obtenerProductoPorId(Integer id) {
        System.out.println("⚠️ Consultando base de datos: ObtenerProductoPorId");
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
                System.out.println("⚠️ Consultando base de datos: ObtenerProductoCompleto");
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
                prod.setPrecioDigital(rsProducto.getFloat("precioDigital"));
                prod.setArchivoComprimido(rsProducto.getString("archivo"));

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


    @Cacheable(value = "productos", key = "'productos'")
    @Override
    public List<ProductoResponseConDestacado> listarProductos() {
        System.out.println("⚠️ Consultando base de datos: listarProductos");
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
        System.out.println("⚠️ Consultando base de datos: obtenerTodosLosIds");
        return productoRepository.findAllIds();
    }

    @Cacheable(value = "productosDestacados", key = "'destacados'")
    @Override
    public List<ProductoConArchivoPrincipalYColoresDTO> obtenerProductosDestacados() {
        System.out.println("⚠️ Consultando base de datos: obtenerProductosDestacados");

        List<ProductoDestacadoEntity> destacados = productoDestacadoRepository.findTop10ByOrderByIdAsc(); // o el orden que prefieras

        return destacados.stream().map(destacado -> {
            ProductoEntity producto = destacado.getProducto();
            ProductoResponseDTO responseDTO = ProductoMapper.toDTO(producto);
            ProductoDTO dto = ProductoDTOMapper.fromResponseDTO(responseDTO);

            List<String> colores = productoColorRepository.findByProductoId(producto.getId()).stream()
                    .map(ProductoColorEntity::getColor)
                    .collect(Collectors.toList());

            ArchivoDTO archivo = productoArchivoRepository.findByProductoIdOrderByOrdenAsc(producto.getId()).stream()
                    .findFirst().map(ArchivoMapper::toArchivoDTO).orElse(null);

            ProductoConArchivoPrincipalYColoresDTO productoCompleto = new ProductoConArchivoPrincipalYColoresDTO();
            productoCompleto.setProducto(dto);
            productoCompleto.setColores(colores);
            productoCompleto.setArchivoPrincipal(archivo);

            return productoCompleto;
        }).toList();
    }




    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "producto", key = "#id"),
    })
    public void eliminarProducto(Integer id) {
        System.out.println("⚠️ Eliminando producto");

        try {
            // Obtener el producto antes de borrarlo para acceder a su categoría
            ProductoEntity producto = productoRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + id));
            Integer categoriaId = producto.getCategoriaId().getId();

            // Borrar imágenes en imgBB
            List<ProductoArchivoEntity> archivos = productoArchivoRepository.findByProductoIdOrderByOrdenAsc(id);
            for (ProductoArchivoEntity archivo : archivos) {
                String imgBBId = archivo.getDeleteUrl();
                if (imgBBId != null) {
                    try {
                        imgBBUploaderService.borrarImagenDeImgBB(imgBBId);
                    } catch (Exception e) {
                        logger.error("Error eliminando imagen en imgBB: " + imgBBId, e);
                    }
                }
            }

            // Eliminar relaciones y producto localmente
            productoRepository.deleteById(id);

            // Refrescar caches manualmente
            productoCacheService.refrescarCacheProducto(id);
            productoCacheService.refrescarTodosLosProductos();
            productoCacheProxyService.precargarProductosDestacados();
            productoCacheProxyService.precargarPorCategoria(categoriaId, PageRequest.of(0, 20));

            // ✅ Actualizar cache de productosPorCategoria
            Cache cachePorCategoria = cacheManager.getCache("productosPorCategoria");
            if (cachePorCategoria != null && categoriaId != null) {
                String categoriaPrefix = categoriaId + "-";
                for (String key : productosPorCategoriaCacheKeys.getKeys()) {
                    if (key.startsWith(categoriaPrefix)) {
                        Page<ProductoResponseDTO> pagina = cachePorCategoria.get(key, Page.class);
                        if (pagina != null) {
                            List<ProductoResponseDTO> listaModificada = pagina.getContent().stream()
                                    .filter(p -> !p.getId().equals(id))
                                    .collect(Collectors.toList());

                            Page<ProductoResponseDTO> paginaActualizada = new PageImpl<>(
                                    listaModificada,
                                    pagina.getPageable(),
                                    pagina.getTotalElements() - 1
                            );
                            cachePorCategoria.put(key, paginaActualizada);
                        }
                    }
                }
            }

            // ✅ productosTodos
            Cache cacheProductosTodos = cacheManager.getCache("productosTodos");
            if (cacheProductosTodos != null) {
                List<ProductoConArchivoPrincipalYColoresDTO> productosTodos = cacheProductosTodos.get("todos", List.class);
                if (productosTodos != null) {
                    List<ProductoConArchivoPrincipalYColoresDTO> actualizados = productosTodos.stream()
                            .filter(p -> !p.getProducto().getId().equals(id))
                            .collect(Collectors.toList());
                    cacheProductosTodos.put("todos", actualizados);
                }
            }

            // ✅ productosDestacados
            Cache cacheProductosDestacados = cacheManager.getCache("productosDestacados");
            if (cacheProductosDestacados != null) {
                List<ProductoConArchivoPrincipalYColoresDTO> productosDestacados = cacheProductosDestacados.get("destacados", List.class);
                if (productosDestacados != null) {
                    List<ProductoConArchivoPrincipalYColoresDTO> actualizados = productosDestacados.stream()
                            .filter(p -> !p.getProducto().getId().equals(id))
                            .collect(Collectors.toList());
                    cacheProductosDestacados.put("destacados", actualizados);
                }
            }

            // ✅ productos
            Cache cacheProductos = cacheManager.getCache("productos");
            if (cacheProductos != null) {
                List<ProductoResponseConDestacado> productos = cacheProductos.get("productos", List.class);
                if (productos != null) {
                    List<ProductoResponseConDestacado> actualizados = productos.stream()
                            .filter(p -> !p.getId().equals(id))
                            .collect(Collectors.toList());
                    cacheProductos.put("productos", actualizados);
                }
            }

            productoCacheService.precargarUltimoProducto();

        } catch (Exception e) {
            logger.error("❌ Error eliminando producto con ID " + id, e);
            throw new RuntimeException("No se pudo eliminar el producto. Ver logs para más detalles.");
        }
    }

    public long contarProductosPorCategoria(Integer categoriaId) {
        return productoRepository.countByCategoriaId_Id(categoriaId);
    }

    @Cacheable(value = "productosPorCategoria", key = "#categoriaId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<ProductoResponseDTO> listarProductosPorCategoria(Integer categoriaId, Pageable pageable) {
        String key = categoriaId + "-" + pageable.getPageNumber() + "-" + pageable.getPageSize();
        productosPorCategoriaCacheKeys.addKey(key);

        System.out.println("⚠️ Consultando base de datos: listarProductosPorCategoria");

        List<ProductoResponseDTO> productos = listarProductosPorCategoriaSP(categoriaId, pageable);
        long total = contarProductosPorCategoria(categoriaId);

        return new PageImpl<>(productos, pageable, total);
    }
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listarProductosPorCategoriaSP(Integer categoriaId, Pageable pageable) {
        System.out.println("⚠️ Consultando base de datos: listarProductosPorCategoriaSP");
        int offset = pageable.getPageNumber() * pageable.getPageSize();
        int limit = pageable.getPageSize();

        List<Object[]> resultados = productoRepository.listarProductosPorCategoriaSP(categoriaId, offset, limit);

        return resultados.stream().map(obj -> {
            Integer id = (Integer) obj[0];
            String nombre = (String) obj[1];
            String descripcion = (String) obj[2];
            float precio = ((Number) obj[3]).floatValue();

            String coloresStr = (String) obj[4];
            String archivosStr = (String) obj[5];

            List<String> colores = coloresStr.isEmpty() ? List.of() : List.of(coloresStr.split(","));

            List<ArchivoDTO> archivos = archivosStr.isEmpty() ? List.of() :
                    Arrays.stream(archivosStr.split("\\|"))
                            .map(s -> {
                                String[] partes = s.split("::");
                                return new ArchivoDTO(
                                        Integer.parseInt(partes[0]),       // id
                                        Integer.parseInt(partes[1]),       // productId
                                        partes[2],                         // linkArchivo
                                        Integer.parseInt(partes[3])        // orden
                                );
                            }).toList();

            return new ProductoResponseDTO(id, nombre, descripcion, precio, colores, archivos);
        }).toList();
    }


    @Override
    @Cacheable(value = "productosTodos", key = "'todos'")
    public List<ProductoConArchivoPrincipalYColoresDTO> obtenerTodosLosProductosSinPaginado() {
        System.out.println("⚠️ Consultando base de datos: obtenerTodosLosProductosSinPaginado");

        System.out.println("⚠️ Antes de llamar a obtenerProductosCompletosSP()");
        List<Object[]> resultados = productoRepository.obtenerProductosCompletosSP();
        System.out.println("⚠️ Resultado del SP: " + (resultados == null ? "null" : resultados.size() + " filas"));

        Map<Integer, ProductoConArchivoPrincipalYColoresDTO> mapaProductos = new HashMap<>();
        Map<Integer, Set<String>> coloresPorProducto = new HashMap<>();

        int filaContador = 0;
        for (Object[] fila : resultados) {
            filaContador++;
            System.out.println("⚠️ Procesando fila #" + filaContador);

            Integer productoId = ((Number) fila[0]).intValue();
            System.out.println("   productoId: " + productoId);

            String nombre = (String) fila[1];
            String descripcion = (String) fila[2];
            Float precio = fila[3] != null ? ((Number) fila[3]).floatValue() : null;
            String color = (String) fila[4];
            String linkArchivo = (String) fila[5];
            Integer orden = fila[6] != null ? ((Number) fila[6]).intValue() : null;

            ProductoConArchivoPrincipalYColoresDTO dto = mapaProductos.get(productoId);
            if (dto == null) {
                System.out.println("   Nuevo DTO para productoId: " + productoId);
                ProductoDTO productoDTO = new ProductoDTO();
                productoDTO.setId(productoId);
                productoDTO.setNombre(nombre);
                productoDTO.setDescripcion(descripcion);
                productoDTO.setPrecio(precio);

                dto = new ProductoConArchivoPrincipalYColoresDTO();
                dto.setProducto(productoDTO);
                dto.setColores(new ArrayList<>());
                dto.setArchivoPrincipal(null);

                mapaProductos.put(productoId, dto);
                coloresPorProducto.put(productoId, new HashSet<>());
            }

            if (color != null) {
                coloresPorProducto.get(productoId).add(color);
                System.out.println("   Agregado color: " + color);
            }

            if (orden != null && (dto.getArchivoPrincipal() == null || orden < dto.getArchivoPrincipal().getOrden())) {
                ArchivoDTO archivoDTO = new ArchivoDTO();
                archivoDTO.setLinkArchivo(linkArchivo);
                archivoDTO.setOrden(orden);
                dto.setArchivoPrincipal(archivoDTO);
                System.out.println("   Seteado archivo principal con orden: " + orden);
            }
        }

        System.out.println("⚠️ Terminada la lectura de filas, asignando colores a DTOs");

        // Asignar lista de colores sin duplicados a cada DTO
        for (Integer productoId : mapaProductos.keySet()) {
            Set<String> coloresSet = coloresPorProducto.get(productoId);
            List<String> coloresList = new ArrayList<>(coloresSet);
            mapaProductos.get(productoId).setColores(coloresList);
            System.out.println("   Producto " + productoId + " tiene colores: " + coloresList);
        }

        System.out.println("⚠️ Finalizado procesamiento de productos, cantidad total: " + mapaProductos.size());

        return new ArrayList<>(mapaProductos.values());
    }






    @Override
    public Page<ProductoConArchivoPrincipalYColoresDTO> obtenerTodosLosProductosConColoresYArchivo(Pageable pageable) {
        List<ProductoConArchivoPrincipalYColoresDTO> listaCompleta = obtenerTodosLosProductosSinPaginado(); // Cacheada

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), listaCompleta.size());

        List<ProductoConArchivoPrincipalYColoresDTO> sublist = listaCompleta.subList(start, end);
        return new PageImpl<>(sublist, pageable, listaCompleta.size());
    }

    @Cacheable(value = "ultimoProducto", key = "'ultimo'")
    public ProductoConArchivoPrincipalYColoresDTO obtenerUltimoProducto() {
        ProductoEntity productoEntity = productoRepository.findTopByOrderByIdDesc()
                .orElseThrow(() -> new NoSuchElementException("No se encontró ningún producto"));

        ProductoResponseDTO responseDTO = ProductoMapper.toDTO(productoEntity);
        return convertirAProductoConArchivoYColores(responseDTO);
    }


    public ProductoConArchivoPrincipalYColoresDTO convertirAProductoConArchivoYColores(ProductoResponseDTO responseDTO) {
        ProductoDTO producto = new ProductoDTO();
        producto.setId(responseDTO.getId());
        producto.setNombre(responseDTO.getNombre());
        producto.setPrecio(responseDTO.getPrecio());

        ArchivoDTO archivoPrincipal = null;
        if (responseDTO.getArchivos() != null && !responseDTO.getArchivos().isEmpty()) {
            archivoPrincipal = responseDTO.getArchivos().get(0);
        }

        ProductoConArchivoPrincipalYColoresDTO dto = new ProductoConArchivoPrincipalYColoresDTO();
        dto.setProducto(producto);
        dto.setArchivoPrincipal(archivoPrincipal);
        dto.setColores(responseDTO.getColores());

        return dto;
    }

}

