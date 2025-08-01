package com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoAprobar;
import com.formaprogramada.ecommerce_backend.Domain.Service.ImgBB.ImgBBUploaderService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.*;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.*;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.*;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Categoria.JpaCategoriaRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.ProductoAprobado.JpaProductoAprobacionRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.ProductoAprobado.JpaProductoArchivoAprobadoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.ProductoAprobado.JpaProductoColorAprobacionRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.ProductoAprobado.JpaProductoDetalleAprobacionRepository;
import com.formaprogramada.ecommerce_backend.Mapper.Producto.ProductoAprobar.ArchivoAprobarMapper;
import com.formaprogramada.ecommerce_backend.Mapper.Producto.ProductoAprobar.ProductoAprobarDTOMapper;
import com.formaprogramada.ecommerce_backend.Mapper.Producto.ProductoAprobar.ProductoAprobarMapper;
import com.formaprogramada.ecommerce_backend.Mapper.Producto.ProductoMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductoAprobadoServiceImpl implements ProductoAprobadoService{
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
    private ProductoAprobadoCacheService productoCacheService;
    @Autowired
    private JpaCategoriaRepository categoriaRepository;
    private ImgBBUploaderService imgBBUploaderService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private CacheManager cacheManager;

    @PersistenceContext
    private EntityManager entityManager;
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
        productoColorAprobacionRepository.deleteByProductoId(productoGuardado.getId());
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
    @Transactional
    public Boolean aprobarProducto(Integer id, String codigoInicial, String versionStr, String seguimiento) {
        System.out.println("Intentando aprobar producto con id = " + id);
        ProductoAprobacionEntity producto = productoAprobacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        ProductoDetalleAprobacionEntity detalleAprobacionEntity = productoDetalleAprobacionRepository.findByProductoId(id);
        List<ProductoColorAprobacionEntity> colorAprobacionEntities = productoColorAprobacionRepository.findByProductoId(id);

        CategoriaEntity categoriaId = producto.getCategoriaId();
        if (categoriaId.getId() == null) {
            categoriaId = categoriaRepository.save(categoriaId);
        } else {
            categoriaId = categoriaRepository.findById(categoriaId.getId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        }

        ProductoRequestConColores dto = new ProductoRequestConColores();
        dto.setCategoriaId(categoriaId.getId());
        List<String> color = new ArrayList<>();
        for (ProductoColorAprobacionEntity colorAprobacionEntity : colorAprobacionEntities) {
            color.add(colorAprobacionEntity.getColor());
        }
        dto.setColores(color);
        dto.setPeso(detalleAprobacionEntity.getPeso());
        dto.setDescripcion(producto.getDescripcion());
        dto.setNombre(producto.getNombre());
        dto.setMaterial(detalleAprobacionEntity.getMaterial());
        dto.setCodigoInicial(codigoInicial);
        dto.setTecnica(detalleAprobacionEntity.getTecnica());

        String[] partes = detalleAprobacionEntity.getDimension().split("x");
        dto.setDimensionAlto(Integer.parseInt(partes[0]));
        dto.setDimensionAncho(Integer.parseInt(partes[1]));
        dto.setDimensionProfundidad(Integer.parseInt(partes[2]));
        dto.setVersion(versionStr);
        dto.setSeguimiento(seguimiento);
        dto.setPrecio(producto.getPrecio());

        byte[] archivo = producto.getArchivo();
        MultipartFile multipartFile = new MockMultipartFile(
                "archivo",
                producto.getNombre(),
                "image/jpeg",
                archivo
        );

        try {
            productoService.crearProducto(dto, multipartFile);

            // --- BORRAR DATOS DE APROBACION ---
            // Primero borrar colores aprobacion
            productoColorAprobacionRepository.deleteAll(colorAprobacionEntities);

            // Borrar detalle aprobacion
            productoDetalleAprobacionRepository.delete(detalleAprobacionEntity);

            // Borrar archivo aprobado (si tienes repositorio para eso)
            productoArchivoRepository.deleteByProductoId(id);

            // Borrar producto aprobacion
            productoAprobacionRepository.delete(producto);

            return true;
        } catch (IOException e) {
            throw new RuntimeException("Error al crear el producto", e);
        }
    }


    @Override
    @Transactional
    public void borrarProducto(Integer id) {
        productoAprobacionRepository.deleteById(id);
    }

    @Override
    public List<ProductoCompletoAprobacionDTO> verProductosaAprobar() {
        List<ProductoAprobacionEntity> productosList = productoAprobacionRepository.findAll();
        List<ProductoCompletoAprobacionDTO> listaEnviar = new ArrayList<>(List.of());

        for (ProductoAprobacionEntity producto: productosList){
            ProductoAprobacionResponseDTO responseDTO = ProductoAprobarMapper.toDTO(producto);
            ProductoAprobacioDTO productoDTO = ProductoAprobarDTOMapper.fromResponseDTO(responseDTO);

            List<String> colores = productoColorAprobacionRepository.findByProductoId(producto.getId())
                    .stream()
                    .map(ProductoColorAprobacionEntity::getColor)
                    .collect(Collectors.toList());


            ProductoAprobacionArchivoDTO archivoPrincipal = productoArchivoRepository.findByProductoIdOrderByOrdenAsc(producto.getId())
                    .stream()
                    .findFirst()
                    .map(ArchivoAprobarMapper::toArchivoDTO)
                    .orElse(null);


            ProductoCompletoAprobacionDTO dto = new ProductoCompletoAprobacionDTO();
            dto.setProducto(productoDTO);
            //dto.setColores(colores);
            //dto.setArchivos((List<ProductoAprobacionArchivoDTO>) archivoPrincipal);

            listaEnviar.add(dto);

        }
        return listaEnviar;
    }

    @Override
    public List<ProductoCompletoAprobacionDTO> verProductosaAprobarDeX(int id) {
        List<ProductoAprobacionEntity> productosList = productoAprobacionRepository.findByUsuarioId_Id(id);
        List<ProductoCompletoAprobacionDTO> listaEnviar = new ArrayList<>(List.of());

        for (ProductoAprobacionEntity producto: productosList){
            ProductoAprobacionResponseDTO responseDTO = ProductoAprobarMapper.toDTO(producto);
            ProductoAprobacioDTO productoDTO = ProductoAprobarDTOMapper.fromResponseDTO(responseDTO);

            List<String> colores = productoColorAprobacionRepository.findByProductoId(producto.getId())
                    .stream()
                    .map(ProductoColorAprobacionEntity::getColor)
                    .collect(Collectors.toList());


            ProductoAprobacionArchivoDTO archivoPrincipal = productoArchivoRepository.findByProductoIdOrderByOrdenAsc(producto.getId())
                    .stream()
                    .findFirst()
                    .map(ArchivoAprobarMapper::toArchivoDTO)
                    .orElse(null);


            ProductoCompletoAprobacionDTO dto = new ProductoCompletoAprobacionDTO();
            dto.setProducto(productoDTO);
            dto.setColores(colores);
            dto.setArchivos((List<ProductoAprobacionArchivoDTO>) archivoPrincipal);

            listaEnviar.add(dto);

        }
        return listaEnviar;
    }
    @Override
    public List<ProductoCompletoAprobacionDTO> verProductoCompleto(int id) {
        ProductoAprobacionEntity producto = productoAprobacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        List<ProductoCompletoAprobacionDTO> listaEnviar = new ArrayList<>();

        ProductoAprobacionResponseDTO responseDTO = ProductoAprobarMapper.toDTO(producto);
        ProductoAprobacioDTO productoDTO = ProductoAprobarDTOMapper.fromResponseDTO(responseDTO);

        // Código desglosado en codigoInicial, version y seguimiento
        String codigo = producto.getCodigo();
        if (codigo != null && codigo.length() >= 7) {
            productoDTO.setCodigoInicial(codigo.substring(0, 3));

            String versionString = codigo.substring(3, 7);
            if (versionString.matches("\\d+")) {
                productoDTO.setVersion(versionString);
            } else {
                System.err.println("Versión inválida: " + versionString);
                productoDTO.setVersion("0");
            }

            if (codigo.length() > 7) {
                productoDTO.setSeguimiento(codigo.substring(7));
            }
        }

        // Convertir byte[] archivo a base64 String y setear en DTO (archivo principal)
        if (producto.getArchivo() != null) {
            String base64Archivo = Base64.getEncoder().encodeToString(producto.getArchivo());
            productoDTO.setArchivo(base64Archivo);
            System.out.println("Archivo size: " + producto.getArchivo().length);
        } else {
            productoDTO.setArchivo(null);
        }

        List<String> colores = productoColorAprobacionRepository.findByProductoId(producto.getId())
                .stream()
                .map(ProductoColorAprobacionEntity::getColor)
                .collect(Collectors.toList());

        List<ProductoAprobacionArchivoDTO> archivos = productoArchivoRepository.findByProductoIdOrderByOrdenAsc(producto.getId())
                .stream()
                .map(ArchivoAprobarMapper::toArchivoDTO)
                .collect(Collectors.toList());

        ProductoDetalleAprobacionEntity detalle = productoDetalleAprobacionRepository.findByProductoId(id);

        String[] partes = detalle.getDimension().split("x");
        productoDTO.setDimensionAlto(partes[0]);
        productoDTO.setDimensionAncho(partes[1]);
        productoDTO.setDimensionProfundidad(partes[2]);
        productoDTO.setMaterial(detalle.getMaterial());
        productoDTO.setTecnica(detalle.getTecnica());
        productoDTO.setPeso(detalle.getPeso());

        ProductoCompletoAprobacionDTO dto = new ProductoCompletoAprobacionDTO();
        dto.setProducto(productoDTO);
        dto.setColores(colores);
        dto.setArchivos(archivos);

        listaEnviar.add(dto);

        return listaEnviar;
    }

    @Override
    public ProductoAprobacionEntity actualizarProductoCompleto(int id, ProductoCompletoAprobacionDTO dto, List<MultipartFile> archivosNuevos, MultipartFile archivoStl) {
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


            ProductoAprobacionEntity productoActualizado = productoAprobacionRepository.save(producto);
            // Actualizar producto_detalle asociado
            ProductoDetalleAprobacionEntity detalles = productoDetalleAprobacionRepository.findByProductoId(producto.getId());
            ProductoDetalleAprobacionEntity detalle;
            if (detalles==null) {
                detalle = new ProductoDetalleAprobacionEntity();
                detalle.setProductoId(producto.getId());
            } else {
                detalle = detalles;
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
            // 3. Borrar colores anteriores
            productoColorAprobacionRepository.deleteByProductoId(productoActualizado.getId());
            productoColorAprobacionRepository.flush(); // asegura que se ejecute el delete en DB
            productoActualizado.getColores().clear(); // opcional si tenés JPA bidireccional
            productoColorAprobacionRepository.deleteByProductoId(productoActualizado.getId());

            // 4. Guardar colores nuevos
            List<String> colores = dto.getColores();
            if (colores != null && !colores.isEmpty()) {
                List<ProductoColorAprobacionEntity> coloresEntities = colores.stream()
                        .map(color -> new ProductoColorAprobacionEntity(0, productoActualizado, color))
                        .toList();
                productoColorAprobacionRepository.saveAll(coloresEntities);
            }

            // 5. Borrar imágenes antiguas (en BDD y en ImgBB)
            List<ProductoArchivoAprobacionEntity> archivosExistentes = productoArchivoRepository.findByProductoIdOrderByOrdenAsc(productoActualizado.getId());


            List<ProductoAprobacionArchivoDTO> urlsQuePermanecen = dto.getArchivos();

            for (ProductoArchivoAprobacionEntity archivo : archivosExistentes) {
                for (ProductoAprobacionArchivoDTO urlNo : urlsQuePermanecen) {
                    if (!Arrays.equals(urlNo.getArchivoImagen(), archivo.getArchivoImagen())){
                            productoArchivoRepository.delete(archivo);
                        }
                }
            }

            // 6. Subir nuevas imágenes y guardar URLs en BDD
            if (archivosNuevos != null && !archivosNuevos.isEmpty()) {
                int orden = 0;
                for (MultipartFile archivoNuevo : archivosNuevos) {
                    ProductoArchivoAprobacionEntity nuevoArchivo = new ProductoArchivoAprobacionEntity();
                    nuevoArchivo.setProducto(productoActualizado);
                    nuevoArchivo.setArchivoImagen(archivoNuevo.getBytes());
                    nuevoArchivo.setOrden(orden++);
                    productoArchivoRepository.save(nuevoArchivo);
                }

            }
            // Convertir productoActualizado a DTO para cache
            ProductoAprobacionResponseDTO responseDTO = ProductoAprobarMapper.toDTO(productoActualizado);
            ProductoAprobacioDTO productoDTO = ProductoAprobarDTOMapper.fromResponseDTO(responseDTO);

            ProductoAprobacionArchivoDTO archivoPrincipal = null;
            if (responseDTO.getArchivos() != null && !responseDTO.getArchivos().isEmpty()) {
                archivoPrincipal = responseDTO.getArchivos().get(0);
            }

            List<String> coloresActualizados = responseDTO.getColores();

            ProductoAprobadoConArchivoPrincipalYColoresDTO productoActualizadoDTO = new ProductoAprobadoConArchivoPrincipalYColoresDTO(
                    productoDTO,
                    archivoPrincipal,
                    coloresActualizados
            );

            productoCacheService.refrescarCacheProducto(id);
            productoCacheService.refrescarTodosLosProductos(); // recalcula productosTodos
            Integer categoriaId = productoActualizado.getCategoriaId().getId();
            Cache cache = cacheManager.getCache("productosTodos");
            if (cache != null) {
                List<ProductoAprobadoConArchivoPrincipalYColoresDTO> lista = cache.get("todos", List.class);
                if (lista != null) {
                    lista = lista.stream()
                            .map(p -> p.getProducto().getId().equals(productoActualizado.getId()) ? productoActualizadoDTO : p)
                            .collect(Collectors.toList());
                    cache.put("todos", lista);
                }
            }
            // 🔄 Actualizar producto en cache "productos"
            Cache cacheProductos = cacheManager.getCache("productos");
            productoCacheService.precargarUltimoProducto();

            return productoActualizado;
        } catch (Exception e) {
            System.err.println("Error en actualizar");
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar el producto", e);
        }
    }

    @Override
    @Cacheable("productosIds")
    public List<Integer> obtenerTodosLosIds() {
        System.out.println("⚠️ Consultando base de datos: obtenerTodosLosIds");
        return productoAprobacionRepository.findAllIds();
    }

    @Override
    public ProductoAprobadoConArchivoPrincipalYColoresDTO obtenerUltimoProducto() {
        ProductoAprobacionEntity productoEntity = productoAprobacionRepository.findTopByOrderByIdDesc()
                .orElseThrow(() -> new NoSuchElementException("No se encontró ningún producto"));

        ProductoAprobacionResponseDTO responseDTO = ProductoAprobarMapper.toDTO(productoEntity);
        return convertirAProductoConArchivoYColores(responseDTO);
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
                    List<ProductoAprobacionArchivoDTO> archivos = new ArrayList<>();
                    while (rsArchivos.next()) {
                        ProductoAprobacionArchivoDTO archivo = new ProductoAprobacionArchivoDTO();
                        archivo.setId(rsArchivos.getInt("id"));
                        archivo.setProductId(rsArchivos.getInt("productId"));
                        archivo.setArchivoImagen(rsArchivos.getBytes("archivoImagen")); // Asegurate que exista en el SP
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


