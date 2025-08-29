package com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoColaborador;

import com.formaprogramada.ecommerce_backend.Domain.Service.ImgBB.ImgBBUploaderService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoArchivoService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.*;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoRequestConColores;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoResponse;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.*;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Categoria.JpaCategoriaRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.JpaProductoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.ProductoAprobado.JpaProductoAprobacionRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.ProductoAprobado.JpaProductoArchivoAprobadoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.ProductoAprobado.JpaProductoColorAprobacionRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.ProductoAprobado.JpaProductoDetalleAprobacionRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Usuario.JpaUsuarioRepository;
import com.formaprogramada.ecommerce_backend.Mapper.Producto.ProductoAprobar.ArchivoAprobarMapper;
import com.formaprogramada.ecommerce_backend.Mapper.Producto.ProductoAprobar.ProductoAprobarDTOMapper;
import com.formaprogramada.ecommerce_backend.Mapper.Producto.ProductoAprobar.ProductoAprobarMapper;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Service

public class ProductoColaboradorServiceImpl implements ProductoColaboradorService{
    @Autowired
    private ProductoService productoService;
    @Autowired
    private JpaProductoAprobacionRepository productoAprobacionRepository;
    @Autowired
    private JpaProductoColorAprobacionRepository productoColorAprobacionRepository;
    @Autowired
    private JpaProductoArchivoAprobadoRepository productoArchivoRepository;
    @Autowired
    private JpaProductoDetalleAprobacionRepository productoDetalleAprobacionRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ProductoColaboradorCacheService productoColaboradorCacheService;
    @Autowired
    private JpaCategoriaRepository categoriaRepository;
    @Autowired
    private JpaUsuarioRepository usuarioRepository;
    private ImgBBUploaderService imgBBUploaderService;
    @Autowired
    private ProductoArchivoService productoArchivoService;
    @Autowired
    private JpaProductoArchivoAprobadoRepository productoArchivoAprobacionRepository;
    @Autowired
    private JpaProductoRepository productoRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private CacheManager cacheManager;
    @Override
    @Transactional
    public Boolean aprobarProducto(Integer id, String codigoInicial, String versionStr, String seguimiento) {
        System.out.println("Intentando aprobar producto con id = " + id);

        try {
            // 1️⃣ Cargar la entidad de aprobación y su detalle
            ProductoAprobacionEntity productoAprob = productoAprobacionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Producto de aprobación no encontrado."));

            ProductoDetalleAprobacionEntity detalleAprob = productoDetalleAprobacionRepository.findByProductoId(id);
            if (detalleAprob == null) {
                throw new RuntimeException("Detalle de producto no encontrado para la aprobación.");
            }

            // Cargar las colecciones perezosas para evitar LazyInitializationException
            productoAprob.getArchivos().size();
            productoAprob.getColores().size();

            // 2️⃣ COPIAR TODOS LOS DATOS ANTES DE ELIMINAR
            Integer usuarioIdCache = productoAprob.getUsuarioId() != null ? productoAprob.getUsuarioId().getId() : null;

            // Copiar datos del archivo principal
            byte[] archivoBytes = productoAprob.getArchivo();
            String nombreProducto = productoAprob.getNombre();

            // Copiar archivos adicionales a una lista temporal
            List<ArchivoTemporal> archivosTemporales = new ArrayList<>();
            for (ProductoArchivoAprobacionEntity archivoAprob : productoAprob.getArchivos()) {
                archivosTemporales.add(new ArchivoTemporal(
                        archivoAprob.getArchivoImagen(),
                        archivoAprob.getOrden(),
                        archivoAprob.getId()
                ));
            }

            // 3️⃣ Preparar el DTO con los datos de la entidad de aprobación
            ProductoRequestConColores dto = new ProductoRequestConColores();
            dto.setCategoriaId(productoAprob.getCategoriaId().getId());
            dto.setColores(productoAprob.getColores().stream()
                    .map(c -> new ColorRequest(c.getColor(), c.getHex()))
                    .toList());
            dto.setPeso(detalleAprob.getPeso());
            dto.setDescripcion(productoAprob.getDescripcion());
            dto.setNombre(productoAprob.getNombre());
            dto.setMaterial(detalleAprob.getMaterial());
            dto.setCodigoInicial(codigoInicial);
            dto.setTecnica(detalleAprob.getTecnica());

            String[] partes = detalleAprob.getDimension().split("x");
            dto.setDimensionAlto(Integer.parseInt(partes[0]));
            dto.setDimensionAncho(Integer.parseInt(partes[1]));
            dto.setDimensionProfundidad(Integer.parseInt(partes[2]));
            dto.setVersion(versionStr);
            dto.setSeguimiento(seguimiento);
            dto.setPrecio(productoAprob.getPrecio());
            dto.setPrecioDigital(productoAprob.getPrecioDigital());

            // 4️⃣ ELIMINACIÓN COMPLETA Y SEGURA usando queries nativas
            try {
                // Eliminar en orden inverso a las dependencias para evitar violaciones de FK
                entityManager.createNativeQuery("DELETE FROM producto_archivos_aprobacion WHERE productid = ?")
                        .setParameter(1, id)
                        .executeUpdate();

                entityManager.createNativeQuery("DELETE FROM producto_colores_aprobacion WHERE productid = ?")
                        .setParameter(1, id)
                        .executeUpdate();

                entityManager.createNativeQuery("DELETE FROM producto_detalle_aprobacion WHERE productoid = ?")
                        .setParameter(1, id)
                        .executeUpdate();

                entityManager.createNativeQuery("DELETE FROM producto_aprobacion WHERE id = ?")
                        .setParameter(1, id)
                        .executeUpdate();

                // Limpiar el contexto de persistencia para evitar referencias obsoletas
                entityManager.flush();
                entityManager.clear();

            } catch (Exception e) {
                throw new RuntimeException("Error al eliminar entidades de aprobación: " + e.getMessage(), e);
            }

            // 5️⃣ Ahora crear el producto final usando los datos copiados
            MultipartFile archivoPrincipal = new MockMultipartFile(
                    "archivo",
                    nombreProducto,
                    "image/jpeg",
                    archivoBytes
            );

            ProductoResponse productoResponse = productoService.crearProducto(dto, archivoPrincipal);
            Integer productoIdFinal = productoResponse.getId();

            // 6️⃣ Subir archivos adicionales usando los datos temporales
            for (ArchivoTemporal archivoTemp : archivosTemporales) {
                MultipartFile file = new MockMultipartFile(
                        "archivo",
                        "archivo_" + archivoTemp.getId(),
                        "image/jpeg",
                        archivoTemp.getBytes()
                );

                productoArchivoService.agregarArchivoConImagen(
                        productoIdFinal,
                        file,
                        archivoTemp.getOrden()
                );
            }

            // 7️⃣ Actualizar la caché
            Cache cacheGeneral = cacheManager.getCache("productoAprobacionList");
            if (cacheGeneral != null) {
                List<ProductoCompletoAprobacionDTO> lista = cacheGeneral.get("ALL", List.class);
                if (lista != null) {
                    lista = lista.stream()
                            .filter(p -> !p.getProducto().getId().equals(id))
                            .collect(Collectors.toCollection(ArrayList::new));
                    cacheGeneral.put("ALL", lista);
                }
            }

            if (usuarioIdCache != null) {
                Cache cacheUsuario = cacheManager.getCache("productoIdAprobar");
                if (cacheUsuario != null) {
                    List<ProductoCompletoAprobacionDTO> lista = cacheUsuario.get(usuarioIdCache, List.class);
                    if (lista != null) {
                        lista = lista.stream()
                                .filter(p -> !p.getProducto().getId().equals(id))
                                .collect(Collectors.toList());
                        cacheUsuario.put(usuarioIdCache, lista);
                    }
                }
            }

            Cache cacheIndividual = cacheManager.getCache("productoCompleto");
            if (cacheIndividual != null) {
                cacheIndividual.evict(id);
            }

            return true;

        } catch (IOException e) {
            throw new RuntimeException("Error al crear el producto o subir archivos", e);
        }
    }

    // Clase auxiliar para almacenar datos temporalmente
    private static class ArchivoTemporal {
        private final byte[] bytes;
        private final Integer orden;
        private final Integer id;

        public ArchivoTemporal(byte[] bytes, Integer orden, Integer id) {
            this.bytes = bytes;
            this.orden = orden;
            this.id = id;
        }

        public byte[] getBytes() { return bytes; }
        public Integer getOrden() { return orden; }
        public Integer getId() { return id; }
    }
    @Override
    public List<ProductoCompletoAprobacionDTO> verProductosaAprobar() {
        Cache cache = cacheManager.getCache("productoAprobacionList");
        List<ProductoCompletoAprobacionDTO> listaCache = cache != null ? cache.get("ALL", List.class) : null;

        if (listaCache != null) {
            System.out.println("[CACHE] Retornando lista de cache");
            return listaCache;
        }

        System.out.println("[CACHE] Cache vacía, cargando desde DB");
        List<ProductoAprobacionEntity> productosList = productoAprobacionRepository.findAll();
        listaCache = new ArrayList<>();

        for (ProductoAprobacionEntity producto : productosList) {
            ProductoAprobacionResponseDTO responseDTO = ProductoAprobarMapper.toDTO(producto);
            ProductoAprobacioDTO productoDTO = ProductoAprobarDTOMapper.fromResponseDTO(responseDTO);

            // Colores
            List<ColorRequest> colores = productoColorAprobacionRepository.findByProductoId(producto.getId())
                    .stream()
                    .map(c -> new ColorRequest(c.getColor(), c.getHex()))
                    .collect(Collectors.toList());

            // Archivos
            List<ProductoAprobacionArchivoDTO> archivos = productoArchivoRepository.findByProductoIdOrderByOrdenAsc(producto.getId())
                    .stream()
                    .map(ArchivoAprobarMapper::toArchivoDTO)
                    .collect(Collectors.toList());

            // DTO completo
            ProductoCompletoAprobacionDTO dto = new ProductoCompletoAprobacionDTO();
            dto.setProducto(productoDTO);
            dto.setColores(colores);
            dto.setArchivos(archivos);

            listaCache.add(dto);
        }

        if (cache != null) cache.put("ALL", listaCache);

        return listaCache;
    }

    public ProductoAprobadoConArchivoPrincipalYColoresDTO convertirAProductoConArchivoYColores(ProductoAprobacionResponseDTO responseDTO) {
        ProductoAprobacioDTO producto = new ProductoAprobacioDTO();
        producto.setId(responseDTO.getId());
        producto.setNombre(responseDTO.getNombre());
        producto.setPrecio(responseDTO.getPrecio());

        ProductoAprobacionArchivoDTO archivoPrincipal = null;
        if (responseDTO.getArchivos() != null && !responseDTO.getArchivos().isEmpty()) {
            archivoPrincipal = responseDTO.getArchivos().get(0);
        }

        ProductoAprobadoConArchivoPrincipalYColoresDTO dto = new ProductoAprobadoConArchivoPrincipalYColoresDTO();
        dto.setProducto(producto);
        dto.setArchivoPrincipal(archivoPrincipal);
        dto.setColores(responseDTO.getColores());

        return dto;
    }
    @Override
    @Transactional
    public ProductoAprobacionResponse crearAprobacionProducto(ProductoAprobacionRequest dto, MultipartFile archivoComprimido) throws IOException {
        UsuarioEntity creador = usuarioRepository.findById(dto.getCreadorId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Construir código concatenado
        String codigoInicial = dto.getCodigoInicial() != null ? dto.getCodigoInicial() : "";
        String versionStr = dto.getVersion() != null ? dto.getVersion() : "";
        String seguimiento = dto.getSeguimiento() != null ? dto.getSeguimiento() : "";
        String codigo = codigoInicial + versionStr + seguimiento;

        // Crear y guardar producto base
        ProductoAprobacionEntity producto = new ProductoAprobacionEntity();
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setPrecioDigital(dto.getPrecioDigital());
        producto.setCodigo(codigo);
        producto.setUsuarioId(creador);

        // Guardar archivo comprimido si existe
        if (archivoComprimido != null && !archivoComprimido.isEmpty()) {
            producto.setArchivo(archivoComprimido.getBytes());
        }

        CategoriaEntity categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        producto.setCategoriaId(categoria);

        ProductoAprobacionEntity productoGuardado = productoAprobacionRepository.save(producto);

        // Guardar detalle producto
        String dimension = dto.getDimensionAlto() + "x" + dto.getDimensionAncho() + "x" + dto.getDimensionProfundidad();
        ProductoDetalleAprobacionEntity detalle = ProductoDetalleAprobacionEntity.builder()
                .productoId(productoGuardado.getId())
                .dimension(dimension)
                .material(dto.getMaterial())
                .tecnica(dto.getTecnica())
                .peso(dto.getPeso() != null ? dto.getPeso() : "")
                .build();
        productoDetalleAprobacionRepository.save(detalle);

        // Guardar colores
        productoColorAprobacionRepository.deleteByProductoId(productoGuardado.getId());
        List<ColorRequest> colores = dto.getColores();

        if (colores != null && !colores.isEmpty()) {
            List<ProductoColorAprobacionEntity> coloresEntities = colores.stream()
                    .map(c -> new ProductoColorAprobacionEntity(
                            0,
                            productoGuardado,
                            c.getNombre(),
                            c.getHex()
                    ))
                    .toList();
            productoColorAprobacionRepository.saveAll(coloresEntities);
        }

        // Recuperar los colores guardados y mapearlos a ColorRequest
        List<ColorRequest> coloresGuardados = productoColorAprobacionRepository.findByProductoId(productoGuardado.getId())
                .stream()
                .map(c -> new ColorRequest(c.getColor(), c.getHex()))
                .toList();

        // Actualizar caches
        ProductoCompletoAprobacionDTO completo = obtenerProductoCompleto(productoGuardado.getId());

        productoColaboradorCacheService.agregarAlCache(productoGuardado);
        productoColaboradorCacheService.agregarProductoAprobarAlCache(creador.getId(), completo);

        // Retornar response con colores como ColorRequest
        return new ProductoAprobacionResponse(productoGuardado, detalle, coloresGuardados);
    }


    @Cacheable(value = "productoCompleto", key = "#id")
    @Override
    public ProductoCompletoAprobacionDTO obtenerProductoCompleto(Integer id) {
        return obtenerProductoCompletoSinCache(id);
    }

    @Override
    @Transactional
    public void borrarProducto(Integer id) {
        // 0️⃣ Obtener usuarioId del producto antes de borrarlo
        Integer usuarioId = productoAprobacionRepository.findById(id)
                .map(p -> p.getUsuarioId().getId()) // obtener el ID del usuario
                .orElse(null);
        if (usuarioId == null) return; // producto no existe


        // 1️⃣ Borrar de BD
        productoAprobacionRepository.deleteById(id);

        // 2️⃣ Actualizar cache general
        Cache cacheGeneral = cacheManager.getCache("productoAprobacionList");
        if (cacheGeneral != null) {
            List<ProductoCompletoAprobacionDTO> lista = cacheGeneral.get("ALL", List.class);
            if (lista != null) {
                List<ProductoCompletoAprobacionDTO> nuevaLista = lista.stream()
                        .filter(p -> !p.getProducto().getId().equals(id))
                        .collect(Collectors.toList());
                cacheGeneral.put("ALL", nuevaLista);
                System.out.println("[CACHE] Producto " + id + " eliminado de cache general (ALL)");
            }
        }


        // 3️⃣ Cache individual
        Cache cacheProducto = cacheManager.getCache("VerProductoCompletoId");
        if (cacheProducto != null) {
            cacheProducto.evict(id);
            System.out.println("[CACHE] Producto " + id + " eliminado de cache individual");
        }

        // 4️⃣ Cache por usuario
        Cache cacheUsuario = cacheManager.getCache("productoIdAprobar");
        if (cacheUsuario != null) {
            List<ProductoCompletoAprobacionDTO> lista = cacheUsuario.get(usuarioId, List.class);
            if (lista != null) {
                List<ProductoCompletoAprobacionDTO> nuevaLista = lista.stream()
                        .filter(p -> !p.getProducto().getId().equals(id))
                        .collect(Collectors.toList());
                cacheUsuario.put(usuarioId, nuevaLista);
                System.out.println("[CACHE] Producto " + id + " eliminado del cache del usuario " + usuarioId);
            }
        }
    }





    @Override
    public List<ProductoCompletoAprobacionDTO> verProductosaAprobarDeX(int id) {
        Cache cache = cacheManager.getCache("productoIdAprobar");
        List<ProductoCompletoAprobacionDTO> listaCache = cache != null ? cache.get(id, List.class) : null;

        if (listaCache != null) {
            System.out.println("[CACHE] Retornando lista de cache para usuarioId=" + id);
            return listaCache;
        }

        System.out.println("[CACHE] Lista no encontrada en cache para usuarioId=" + id + ", cargando desde BD");

        List<ProductoAprobacionEntity> productosList = productoAprobacionRepository.findByUsuarioId_Id(id);
        List<ProductoCompletoAprobacionDTO> listaEnviar = productosList.stream().map(producto -> {
            ProductoAprobacionResponseDTO responseDTO = ProductoAprobarMapper.toDTO(producto);
            ProductoAprobacioDTO productoDTO = ProductoAprobarDTOMapper.fromResponseDTO(responseDTO);

            ProductoCompletoAprobacionDTO dto = new ProductoCompletoAprobacionDTO();
            dto.setProducto(productoDTO);

            // Colores como ColorRequest
            List<ColorRequest> colores = productoColorAprobacionRepository.findByProductoId(producto.getId())
                    .stream()
                    .map(c -> new ColorRequest(c.getColor(), c.getHex()))
                    .toList();
            dto.setColores(colores);

            ProductoAprobacionArchivoDTO archivoPrincipal = productoArchivoRepository
                    .findByProductoIdOrderByOrdenAsc(producto.getId())
                    .stream()
                    .findFirst()
                    .map(ArchivoAprobarMapper::toArchivoDTO)
                    .orElse(null);
            dto.setArchivos(archivoPrincipal != null ? List.of(archivoPrincipal) : Collections.emptyList());

            return dto;
        }).collect(Collectors.toCollection(ArrayList::new));

        if (cache != null) {
            cache.put(id, listaEnviar);
            System.out.println("[CACHE] Lista agregada al cache para usuarioId=" + id);
        }

        return listaEnviar;
    }

    @Override
    @Transactional
    public ProductoAprobacionEntity actualizarProductoCompleto(int id, ProductoCompletoAprobacionDTO dto, List<MultipartFile> archivosNuevos, MultipartFile archivoComprimido,         String eliminarArchivoComprimido
    ) {
        System.out.println("DTO Archivos:");
        dto.getArchivos().forEach(a -> System.out.println(a.getArchivoImagen()));
        System.out.println("DTO Colores:");
        dto.getColores().forEach(System.out::println);

        try {
            System.out.println("⚠️ Actualizando producto");
            // 1. Buscar producto existente
            ProductoAprobacionEntity producto = productoAprobacionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            Optional<ProductoAprobacionEntity> productoConMismoNombre = productoAprobacionRepository.findByNombre(dto.getProducto().getNombre());

            if (productoConMismoNombre.isPresent() && !productoConMismoNombre.get().getId().equals(producto.getId())) {
                throw new RuntimeException("Ya existe un producto con ese nombre.");
            }

            // 2. Actualizar campos básicos
            producto.setNombre(dto.getProducto().getNombre());
            producto.setDescripcion(dto.getProducto().getDescripcion());
            if (dto.getProducto().getPrecio() != null) {
                producto.setPrecio(dto.getProducto().getPrecio());
            }

            if (dto.getProducto().getPrecioDigital() != null) {
                producto.setPrecioDigital(dto.getProducto().getPrecioDigital());
            }
            if (dto.getProducto().getCategoriaId() != null) {
                CategoriaEntity categoria = categoriaRepository.findById(dto.getProducto().getCategoriaId())
                        .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
                producto.setCategoriaId(categoria);
            }

            // Construir código concatenado seguro
            String codigoInicial = dto.getProducto().getCodigoInicial() != null ? dto.getProducto().getCodigoInicial() : "";
            String versionStr = dto.getProducto().getVersion() != null ? dto.getProducto().getVersion() : "";
            String seguimiento = dto.getProducto().getSeguimiento() != null ? dto.getProducto().getSeguimiento() : "";
            System.out.println("CodigoInicial: '" + codigoInicial + "' (" + codigoInicial.length() + ")");
            System.out.println("Version: '" + versionStr + "' (" + versionStr.length() + ")");
            System.out.println("Seguimiento: '" + seguimiento + "' (" + seguimiento.length() + ")");
            System.out.println("Codigo concatenado: '" + (codigoInicial + versionStr + seguimiento) + "' (" + (codigoInicial + versionStr + seguimiento).length() + ")");

            producto.setCodigo(codigoInicial + versionStr + seguimiento);

            // Guardar archivo ZIP si existe y cambió
            // Si el usuario pidió eliminar el archivo comprimido
            if ("true".equalsIgnoreCase(eliminarArchivoComprimido)) {
                producto.setArchivo(null);
            }
            // Si no lo eliminó pero envió un nuevo archivo, actualizamos
            else if (archivoComprimido != null && !archivoComprimido.isEmpty()) {
                byte[] archivoActual = producto.getArchivo();
                byte[] archivoNuevo = archivoComprimido.getBytes();

                if (archivoActual == null || !Arrays.equals(archivoActual, archivoNuevo)) {
                    producto.setArchivo(archivoNuevo);
                }
            }


            ProductoAprobacionEntity productoActualizado = productoAprobacionRepository.save(producto);

            // Actualizar producto_detalle asociado
            ProductoDetalleAprobacionEntity detalle = productoDetalleAprobacionRepository.findByProductoId(producto.getId());
            if (detalle == null) {
                detalle = new ProductoDetalleAprobacionEntity();
                detalle.setProductoId(producto.getId());
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

            productoDetalleAprobacionRepository.save(detalle);

            // 3. Borrar colores anteriores y agregar nuevos
            productoColorAprobacionRepository.deleteByProductoId(productoActualizado.getId());
            productoColorAprobacionRepository.flush();
// Limpieza JPA bidireccional si aplica
            productoActualizado.getColores().clear();

// Ahora dto.getColores() devuelve List<ColorRequest>
            List<ColorRequest> colores = dto.getColores();
            if (colores != null && !colores.isEmpty()) {
                List<ProductoColorAprobacionEntity> coloresEntities = colores.stream()
                        .map(c -> new ProductoColorAprobacionEntity(
                                0,
                                productoActualizado,
                                c.getNombre(),  // nombre del color
                                c.getHex()      // código hexadecimal
                        ))
                        .toList();
                productoColorAprobacionRepository.saveAll(coloresEntities);
            }

// --- Imágenes: borrar TODO y reinsertar las que quedan + nuevas ---
// Borro todas las existentes (más simple y determinista)
            productoArchivoRepository.deleteByProductoId(productoActualizado.getId());
            productoArchivoRepository.flush(); // forzar borrado ahora (opcional dentro de la misma tx)

// Archivos que vienen en el DTO y NO están marcados como eliminados
            List<ProductoAprobacionArchivoDTO> archivosDto = dto.getArchivos() == null
                    ? Collections.emptyList()
                    : dto.getArchivos().stream()
                    .filter(a -> !Boolean.TRUE.equals(a.getEliminado()))
                    .collect(Collectors.toList());

// 1) Reinsertar los archivos (base64) que vienen en el DTO (manteniendo orden deseado)
            int orden = 0;
            for (ProductoAprobacionArchivoDTO dtoArchivo : archivosDto) {
                ProductoArchivoAprobacionEntity nuevo = new ProductoArchivoAprobacionEntity();
                nuevo.setProducto(productoActualizado);
                if (dtoArchivo.getArchivoImagen() != null) {
                    nuevo.setArchivoImagen(Base64.getDecoder().decode(dtoArchivo.getArchivoImagen()));
                }
                nuevo.setOrden(orden++);
                productoArchivoRepository.save(nuevo);
            }

// 2) Añadir los archivos nuevos MultipartFile (si los hay), continuando el orden
            if (archivosNuevos != null && !archivosNuevos.isEmpty()) {
                for (MultipartFile mf : archivosNuevos) {
                    ProductoArchivoAprobacionEntity nuevo = new ProductoArchivoAprobacionEntity();
                    nuevo.setProducto(productoActualizado);
                    nuevo.setArchivoImagen(mf.getBytes());
                    nuevo.setOrden(orden++);
                    productoArchivoRepository.save(nuevo);
                }
            }



            // Obtener el producto completo actualizado directamente
            ProductoCompletoAprobacionDTO dtoCompleto = obtenerProductoCompletoSinCache(productoActualizado.getId());

            if (dtoCompleto != null) {
                // Actualizar cache del producto completo por ID
                Cache cacheProducto = cacheManager.getCache("productoCompleto");
                if (cacheProducto != null) {
                    cacheProducto.put(productoActualizado.getId(), dtoCompleto);
                }

                // Actualizar cache por usuario (producto a aprobar)
                productoColaboradorCacheService.agregarProductoAprobarAlCache(
                        productoActualizado.getUsuarioId().getId(), dtoCompleto);

                // Actualizar cache general de productos
                productoColaboradorCacheService.agregarAlCache(productoActualizado);
            }

            return productoActualizado;


        } catch (Exception e) {
            System.err.println("Error en actualizar");
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar el producto", e);
        }
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
                prod.setPrecioDigital(rsProducto.getFloat("precioDigital"));
                prod.setArchivo(rsProducto.getString("archivo"));
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
                    List<ColorRequest> colores = new ArrayList<>();
                    while (rsColores.next()) {
                        String nombre = rsColores.getString("Color");   // nombre del color
                        String hex = rsColores.getString("Hex");        // código hexadecimal
                        colores.add(new ColorRequest(nombre, hex));
                    }
                    resultado.setColores(colores);
                }

                // Archivos
                if (cs.getMoreResults()) {
                    try {
                        ResultSet rsArchivos = cs.getResultSet();
                        List<ProductoAprobacionArchivoDTO> archivos = new ArrayList<>();
                        while (rsArchivos.next()) {
                            ProductoAprobacionArchivoDTO archivo = new ProductoAprobacionArchivoDTO();
                            archivo.setId(rsArchivos.getInt("id"));
                            archivo.setProductId(rsArchivos.getInt("productId"));
                            byte[] bytesImagen = rsArchivos.getBytes("archivoImagen");
                            String base64Imagen = bytesImagen != null ? Base64.getEncoder().encodeToString(bytesImagen) : null;
                            archivo.setArchivoImagen(base64Imagen);
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
    public byte[] crearZipCompatible(byte[] contenidoArchivo, String nombreArchivo) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.setLevel(Deflater.DEFAULT_COMPRESSION); // compresión estándar compatible
            ZipEntry entry = new ZipEntry(nombreArchivo);
            zos.putNextEntry(entry);
            zos.write(contenidoArchivo);
            zos.closeEntry();
        }
        return baos.toByteArray();
    }
}
