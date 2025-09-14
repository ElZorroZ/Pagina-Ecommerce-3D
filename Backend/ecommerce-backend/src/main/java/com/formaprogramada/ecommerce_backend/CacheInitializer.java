package com.formaprogramada.ecommerce_backend;

import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.Pedido;
import com.formaprogramada.ecommerce_backend.Domain.Service.Carrito.CarritoCacheProxyService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Categoria.CategoriaCacheProxy;
import com.formaprogramada.ecommerce_backend.Domain.Service.Colaborador.ColaboradorCacheProxyService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Pedido.PedidoServiceImpl;
import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoCacheProxyService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoCacheService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoColaborador.ProductoColaboradorCacheService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoColaborador.ProductoColaboradorService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoColaborador.ProductoColaboradorServiceImpl;
import com.formaprogramada.ecommerce_backend.Domain.Service.Review.ReviewServiceImpl;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Pedido.PedidoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Pedido.PedidoUsuarioDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ProductoCompletoAprobacionDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Pedido.PedidoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoAprobacionEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Review.ReviewEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Carrito.JpaCarritoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Categoria.JpaCategoriaRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Pedido.JpaPedidoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.ProductoAprobado.JpaProductoAprobacionRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Review.JpaReviewRepository;
import com.formaprogramada.ecommerce_backend.Mapper.Pedido.PedidoMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class CacheInitializer {


    private final ProductoCacheService productoCacheService;
    @Autowired
    private PedidoServiceImpl pedidoService; // inyectamos tu service de pedidos
    @Autowired
    private ProductoCacheProxyService productoCacheProxyService;
    @Autowired
    private JpaCategoriaRepository jpaCategoriaRepository;
    @Autowired
    private JpaPedidoRepository jpaPedidoRepository;
    @Autowired
    private CategoriaCacheProxy categoriaCacheProxyService;
    @Autowired
    private ColaboradorCacheProxyService colaboradorCacheProxyService;
    @Autowired
    private CarritoCacheProxyService carritoCacheProxyService;
    @Autowired
    private JpaCarritoRepository carritoRepository;
    @Autowired
    private ProductoColaboradorCacheService cacheService;
    @Autowired
    private ProductoColaboradorService productoColaborador;
    @Autowired
    private JpaProductoAprobacionRepository productoAprobacionRepository;
    @Autowired
    private ReviewServiceImpl reviewService;
    @Autowired
    private JpaReviewRepository reviewRepository;
    @Autowired
    private CacheManager cacheManager;

    public CacheInitializer(ProductoCacheService productoCacheService,
                            ProductoColaboradorService productoColaborador,
                            ProductoColaboradorCacheService cacheService
             ) {
        this.productoColaborador = productoColaborador;
        this.productoCacheService = productoCacheService;
        this.cacheService = cacheService;

    }
    public CacheManager getCacheManager() {
        return cacheManager;
    }
    @EventListener(ContextRefreshedEvent.class)
    @Transactional// Este método ahora sí correrá dentro de una transacción
    public void cargarCacheAlIniciar() {

        List<Integer> productIds = reviewRepository.findAll()
                .stream()
                .map(ReviewEntity::getProductId)
                .distinct()
                .toList();

        for (Integer productId : productIds) {
            reviewService.listarReviewsConRespuestas(productId); // esto llena el cache interno
        }

        System.out.println("[CACHE INIT] Precargadas " + productIds.size() + " listas de reviews en cache.");
        //Colaboradores
        colaboradorCacheProxyService.precargarColaboradores();
        //  los productos
        List<ProductoAprobacionEntity> todosProductos = productoAprobacionRepository.findAll();
        for (ProductoAprobacionEntity prod : todosProductos) {
            // 1️⃣ Cache de lista general
            cacheService.agregarAlCache(prod);

            // 2️⃣ Cache por usuario (producto a aprobar)
            try {
                ProductoCompletoAprobacionDTO completo = productoColaborador.obtenerProductoCompletoSinCache(prod.getId());
                if (completo != null && completo.getProducto() != null) {
                    cacheService.agregarProductoAprobarAlCache(prod.getUsuarioId().getId(), completo);

                    // 3️⃣ Cache por producto completo
                    cacheService.agregarProductoCompletoAlCache(prod.getId(), completo);
                }
            } catch (Exception e) {
                System.err.println("[CACHE INIT] Error cargando producto " + prod.getId() + ": " + e.getMessage());
            }
        }

        System.out.println("[CACHE INIT] Precarga completada. Productos en cache: " + todosProductos.size());

        //Carrito
        List<Integer> usuariosConCarrito = carritoRepository.obtenerIdsUsuariosConCarrito();
        carritoCacheProxyService.precargarTodosLosCarritos(usuariosConCarrito);
        carritoCacheProxyService.precargarTodosLosCarritosCompletos(usuariosConCarrito);
        // 1️⃣ Cache global de pedidos ("pedidos")
        List<PedidoDTO> todosPedidosDTO = pedidoService.verPedidos();
        pedidoService.getCacheManager().getCache("pedidos").put("all", todosPedidosDTO);
        System.out.println("[CACHE INIT] Cache global de pedidos precargada con " + todosPedidosDTO.size() + " pedidos.");

        // 2️⃣ Cache de pedidos individuales ("pedido" y "pedidoMpId") y por MercadoPago ("pedidoMp")
        for (PedidoDTO dto : todosPedidosDTO) {

            // Cache "pedido"
            PedidoUsuarioDTO pedidoUsuarioDTO = pedidoService.verPedido(dto.getId());
            pedidoService.getCacheManager().getCache("pedido").put(dto.getId(), pedidoUsuarioDTO);

            // Cache "pedidoMpId"
            Pedido pedido = pedidoService.obtenerPedidoPorId(dto.getId());
            if (pedido != null) {
                pedidoService.getCacheManager().getCache("pedidoMpId").put(dto.getId(), pedido);

                // Cache "pedidoMp" si tiene externalPaymentId
                if (pedido.getExternalPaymentId() != null) {
                    pedidoService.getCacheManager().getCache("pedidoMp").put(pedido.getExternalPaymentId(), pedido);
                }
            }

            // Cache "pedidosUsuario"
            Cache pedidosUsuarioCache = pedidoService.getCacheManager().getCache("pedidosUsuario");
            List<PedidoDTO> pedidosUsuario = pedidosUsuarioCache.get(dto.getUsuarioId(), List.class);
            if (pedidosUsuario == null) pedidosUsuario = new ArrayList<>();
            pedidosUsuario.add(dto);
            pedidosUsuarioCache.put(dto.getUsuarioId(), pedidosUsuario);

            List<Integer> usuariosConPedidos = jpaPedidoRepository.findAllUsuariosConPedidos()
                    .stream()
                    .distinct()
                    .toList();

            for (Integer idUsuario : usuariosConPedidos) {
                List<PedidoEntity> pedidos = jpaPedidoRepository.findPedidosConProductosPorUsuario(idUsuario);
                List<PedidoDTO> pedidosDTO = PedidoMapper.toDTOList(pedidos);
                pedidoService.getCacheManager().getCache("pedidosUsuario").put(idUsuario, pedidosDTO);
            }


        }

        System.out.println("[CACHE INIT] Todos los caches de pedidos precargados correctamente.");

        //Categorias
        // Precargar lista de categorías
        categoriaCacheProxyService.precargarCategoriasLista();

        // Precargar cada categoría individual
        List<Integer> ids = jpaCategoriaRepository.findAllIds();
        for (Integer id : ids) {
            categoriaCacheProxyService.precargarCategoriaIndividual(id);
        }
        categoriaCacheProxyService.precargarCategoriasCombo();

        //Productos
        productoCacheProxyService.precargarProductosTodos();
        productoCacheService.precargarCacheProductos();
        productoCacheService.precargarOtrosCaches();
        productoCacheService.precargarCacheProductosTodos(PageRequest.of(0, 20));
        productoCacheProxyService.precargarProductosDestacados();
        productoCacheProxyService.precargarProductosResumen();
        productoCacheService.precargarUltimoProducto();
        // Precargar cache de productosPorCategoria para todas las categorías con bucle
        List<Integer> categoriasIds = jpaCategoriaRepository.findAllIds();
        for (Integer categoriaId : categoriasIds) {
            productoCacheProxyService.precargarPorCategoria(categoriaId, PageRequest.of(0, 20));
        }
    }

}
