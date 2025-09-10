package com.formaprogramada.ecommerce_backend.Domain.Service.Pedido;

import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.Pedido;
import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.PedidoProducto;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Pedido.PedidoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Pedido.PedidoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Pedido.PedidoUsuarioDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioUpdate;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioUpdatePedido;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Pedido.PedidoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Pedido.PedidoProductoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Pedido.JpaPedidoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.JpaProductoRepository;
import com.formaprogramada.ecommerce_backend.Mapper.Pedido.PedidoMapper;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PedidoServiceImpl implements PedidoService {
    private PedidoRepository pedidoRepository;
    @Autowired
    private JpaPedidoRepository jpaPedidoRepository;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private JpaProductoRepository jpaProductoRepository;
    public CacheManager getCacheManager() {
        return cacheManager;
    }
    private Cache getCache() {
        return cacheManager.getCache("pedido");
    }

    @Override
    public Pedido CrearPedido(List<PedidoProducto> lista, int idUsuario) {
        Pedido pedido = pedidoRepository.CrearPedido(lista, idUsuario);

        // Guardar en cache "pedido"
        cacheManager.getCache("pedido")
                .put(pedido.getId(), pedidoRepository.verPedido(pedido.getId()));

        // Guardar en cache "pedidoMpId"
        cacheManager.getCache("pedidoMpId")
                .put(pedido.getId(), obtenerPedidoPorId(pedido.getId()));

        // Actualizar lista "pedidos"
        Cache pedidosCache = cacheManager.getCache("pedidos");
        if (pedidosCache != null) {
            PedidoEntity entity = jpaPedidoRepository.findById(pedido.getId())
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

            PedidoDTO dto = PedidoMapper.toDTO(entity);

            List<PedidoDTO> pedidos = pedidosCache.get("all", List.class);
            if (pedidos != null) {
                pedidos.add(dto);
                pedidosCache.put("all", pedidos);
            }
        }

        // Actualizar lista "pedidosUsuario"
        Cache pedidosUsuarioCache = cacheManager.getCache("pedidosUsuario");
        if (pedidosUsuarioCache != null) {
            List<PedidoDTO> pedidosUsuario = pedidosUsuarioCache.get(idUsuario, List.class);
            if (pedidosUsuario != null) {
                PedidoEntity entity = jpaPedidoRepository.findById(pedido.getId())
                        .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
                PedidoDTO dto = PedidoMapper.toDTO(entity);

                pedidosUsuario.add(dto);
                pedidosUsuarioCache.put(idUsuario, pedidosUsuario);
            }
        }

        return pedido;
    }




    @Override
    public void BorrarPedido(int id) {
        // Primero obtenemos el pedido para saber el usuario
        PedidoEntity entity = jpaPedidoRepository.findById(id)
                .orElse(null);

        pedidoRepository.BorrarPedido(id);

        // Eliminar de cache "pedido"
        cacheManager.getCache("pedido").evict(id);

        // Eliminar de cache "pedidoMpId"
        cacheManager.getCache("pedidoMpId").evict(id);

        // Eliminar de cache "pedidoMp" si existía
        if (entity != null && entity.getExternalPaymentId() != null) {
            cacheManager.getCache("pedidoMp").evict(entity.getExternalPaymentId());
        }

        // Actualizar lista "pedidos" (global)
        Cache pedidosCache = cacheManager.getCache("pedidos");
        if (pedidosCache != null) {
            List<PedidoDTO> pedidos = pedidosCache.get("all", List.class);
            if (pedidos != null) {
                pedidos.removeIf(p -> p.getId() == id);
                pedidosCache.put("all", pedidos);
            }
        }

        // Actualizar lista "pedidosUsuario"
        if (entity != null && entity.getUsuarioId() != null) {
            Cache pedidosUsuarioCache = cacheManager.getCache("pedidosUsuario");
            if (pedidosUsuarioCache != null) {
                List<PedidoDTO> pedidosUsuario = pedidosUsuarioCache.get(entity.getUsuarioId().getId(), List.class);
                if (pedidosUsuario != null) {
                    pedidosUsuario.removeIf(p -> p.getId() == id);
                    pedidosUsuarioCache.put(entity.getUsuarioId().getId(), pedidosUsuario);
                }
            }
        }
    }


    @Override
    public void ModificarPedido(UsuarioUpdatePedido usuario) {
        // En realidad esto modifica el usuario, no un pedido
        pedidoRepository.ModificarPedido(usuario);

        // ⚠️ No refresques cache de pedidos acá, porque no hay pedido todavía
    }


    @Override
    public void CambiarEstado(String estado, int id) {
        pedidoRepository.CambiarEstado(estado, id);

        // Refrescar cache "pedido" usando verPedido (PedidoUsuarioDTO)
        PedidoUsuarioDTO actualizado = pedidoRepository.verPedido(id);
        cacheManager.getCache("pedido").put(id, actualizado);

        // Refrescar cache "pedidoMpId"
        cacheManager.getCache("pedidoMpId").put(id, obtenerPedidoPorId(id));

        // Refrescar cache "pedidoMp" si existe
        jpaPedidoRepository.findById(id).ifPresent(entity -> {
            if (entity.getExternalPaymentId() != null) {
                Pedido pedido = obtenerPedidoPorMercadoPagoId(entity.getExternalPaymentId());
                cacheManager.getCache("pedidoMp").put(entity.getExternalPaymentId(), pedido);
            }
        });

        // Actualizar lista "pedidos" (global)
        Cache pedidosCache = cacheManager.getCache("pedidos");
        if (pedidosCache != null) {
            List<PedidoDTO> pedidos = pedidosCache.get("all", List.class);
            if (pedidos != null) {
                PedidoEntity entity = jpaPedidoRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
                PedidoDTO dto = PedidoMapper.toDTO(entity);

                for (int i = 0; i < pedidos.size(); i++) {
                    if (pedidos.get(i).getId() == id) {
                        pedidos.set(i, dto); // actualizo la lista global
                        break;
                    }
                }
                pedidosCache.put("all", pedidos);
            }
        }

        // Actualizar lista "pedidosUsuario"
        jpaPedidoRepository.findById(id).ifPresent(entity -> {
            if (entity.getUsuarioId() != null) {
                Cache pedidosUsuarioCache = cacheManager.getCache("pedidosUsuario");
                if (pedidosUsuarioCache != null) {
                    List<PedidoDTO> pedidosUsuario = pedidosUsuarioCache.get(entity.getUsuarioId().getId(), List.class);
                    if (pedidosUsuario != null) {
                        PedidoDTO dto = PedidoMapper.toDTO(entity);
                        for (int i = 0; i < pedidosUsuario.size(); i++) {
                            if (pedidosUsuario.get(i).getId() == id) {
                                pedidosUsuario.set(i, dto); // actualizo la lista del usuario
                                break;
                            }
                        }
                        pedidosUsuarioCache.put(entity.getUsuarioId().getId(), pedidosUsuario);
                    }
                }
            }
        });
    }



    @Override
    public void guardarMercadoPagoId(String pedidoId, String mpId) {
        int id = Integer.parseInt(pedidoId);
        PedidoEntity entity = jpaPedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        entity.setExternalPaymentId(mpId);
        entity.setPaymentProvider("MERCADOPAGO");
        jpaPedidoRepository.save(entity);

        // Guardar en cache "pedidoMp"
        Pedido pedido = obtenerPedidoPorMercadoPagoId(mpId);
        cacheManager.getCache("pedidoMp").put(mpId, pedido);

        // Refrescar cache "pedidoMpId"
        cacheManager.getCache("pedidoMpId").put(id, obtenerPedidoPorId(id));

        // Refrescar lista "pedidos" (global)
        Cache pedidosCache = cacheManager.getCache("pedidos");
        if (pedidosCache != null) {
            List<PedidoDTO> pedidos = pedidosCache.get("all", List.class);
            if (pedidos != null) {
                PedidoDTO dto = PedidoMapper.toDTO(entity);
                for (int i = 0; i < pedidos.size(); i++) {
                    if (pedidos.get(i).getId() == id) {
                        pedidos.set(i, dto);
                        break;
                    }
                }
                pedidosCache.put("all", pedidos);
            }
        }

        // Refrescar lista "pedidosUsuario"
        if (entity.getUsuarioId() != null) {
            Cache pedidosUsuarioCache = cacheManager.getCache("pedidosUsuario");
            if (pedidosUsuarioCache != null) {
                List<PedidoDTO> pedidosUsuario = pedidosUsuarioCache.get(entity.getUsuarioId().getId(), List.class);
                if (pedidosUsuario != null) {
                    PedidoDTO dto = PedidoMapper.toDTO(entity);
                    for (int i = 0; i < pedidosUsuario.size(); i++) {
                        if (pedidosUsuario.get(i).getId() == id) {
                            pedidosUsuario.set(i, dto);
                            break;
                        }
                    }
                    pedidosUsuarioCache.put(entity.getUsuarioId().getId(), pedidosUsuario);
                }
            }
        }
    }


    @Override
    public PedidoUsuarioDTO verPedido(int id) {
        Cache cache = getCache();
        PedidoUsuarioDTO pedido = cache.get(id, PedidoUsuarioDTO.class);
        if (pedido == null) {
            pedido = pedidoRepository.verPedido(id);
            if (pedido != null) {
                cache.put(id, pedido);
            }
        }
        return pedido;
    }
    @Override
    public Pedido obtenerPedidoPorMercadoPagoId(String externalPaymentId) {
        Cache cache = cacheManager.getCache("pedidoMp");
        Pedido pedido = cache.get(externalPaymentId, Pedido.class);

        if (pedido == null) {
            PedidoEntity entity = jpaPedidoRepository.findByExternalPaymentId(externalPaymentId)
                    .orElse(null);
            if (entity == null) return null;

            pedido = new Pedido();
            pedido.setId(entity.getId());
            pedido.setFechaPedido(entity.getFechaPedido());
            pedido.setTotal(entity.getTotal());
            pedido.setUsuarioId(entity.getUsuarioId().getId());
            pedido.setEstado(entity.getEstado());

            // Guardar en cache por externalPaymentId
            cache.put(externalPaymentId, pedido);
        }
        return pedido;
    }

    @Override
    public Pedido obtenerPedidoPorId(Integer pedidoId) {
        Cache cache = cacheManager.getCache("pedidoMpId");
        Pedido pedido = cache.get(pedidoId, Pedido.class);

        if (pedido == null) {
            Optional<PedidoEntity> pedidoEntityOpt = jpaPedidoRepository.findById(pedidoId);
            if (pedidoEntityOpt.isEmpty()) {
                System.out.println("❌ No se encontró pedido con ID: " + pedidoId);
                return null;
            }

            PedidoEntity entity = pedidoEntityOpt.get();
            pedido = new Pedido();
            pedido.setId(entity.getId());
            pedido.setTotal(entity.getTotal());
            pedido.setFechaPedido(entity.getFechaPedido());
            pedido.setEstado(entity.getEstado());
            pedido.setUsuarioId(entity.getUsuarioId() != null ? entity.getUsuarioId().getId() : 0);

            // Guardar en cache
            cache.put(pedidoId, pedido);
            System.out.println("✅ Pedido encontrado y cacheado: " + pedido.getId());
        }
        return pedido;
    }


    @SuppressWarnings("unchecked")
    @Cacheable(value = "pedidos", key = "'all'")
    @Transactional(readOnly = true)
    @Override
    public List<PedidoDTO> verPedidos() {
        List<PedidoEntity> lista = jpaPedidoRepository.findAll();
        return PedidoMapper.toDTO(lista);
    }



    @Cacheable(value = "pedidosUsuario", key = "#idUsuario")
    @Override
    public List<PedidoDTO> verPedidosDeUsuario(int idUsuario) {
        // 1️⃣ Traer pedidos con productos
        List<PedidoEntity> pedidos = jpaPedidoRepository.findPedidosConProductosPorUsuario(idUsuario);

        // 2️⃣ Obtener todos los IDs de productos para traer sus archivos
        List<Integer> productoIds = pedidos.stream()
                .flatMap(p -> p.getProductos().stream())
                .map(pp -> pp.getProducto().getId())
                .distinct()
                .toList();

        // 3️⃣ Traer productos con archivos
        List<ProductoEntity> productosConArchivos = jpaProductoRepository.findProductosConArchivos(productoIds);

        // 4️⃣ Mapear de nuevo los archivos a cada producto del pedido
        Map<Integer, ProductoEntity> mapProductos = productosConArchivos.stream()
                .collect(Collectors.toMap(ProductoEntity::getId, p -> p));

        for (PedidoEntity pedido : pedidos) {
            for (PedidoProductoEntity pp : pedido.getProductos()) {
                pp.setProducto(mapProductos.get(pp.getProducto().getId()));
            }
        }

        // 5️⃣ Convertir a DTO
        return PedidoMapper.toDTOList(pedidos);
    }




}
