package com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoColaborador;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ProductoAprobacionResponseDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ProductoCompletoAprobacionDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoAprobacionEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.ProductoAprobado.JpaProductoAprobacionRepository;
import com.formaprogramada.ecommerce_backend.Mapper.Producto.ProductoAprobar.ProductoAprobarMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service

@RequiredArgsConstructor
public class ProductoColaboradorCacheService {

    private final CacheManager cacheManager;
    private final JpaProductoAprobacionRepository productoAprobacionRepository;
    private ProductoColaboradorService productoColaboradorService; // inyectar aquí


    @Autowired
    public void setProductoColaboradorService(@Lazy ProductoColaboradorService productoColaboradorService) {
        this.productoColaboradorService = productoColaboradorService;
    }
    @Cacheable(value = "productoAprobacionList", key = "'ALL'")
    public List<ProductoAprobacionEntity> obtenerTodos() {
        return productoAprobacionRepository.findAll();
    }

    // Actualiza lista general sin invalidar
    public void agregarAlCache(ProductoAprobacionEntity nuevoProducto) {
        Cache cache = cacheManager.getCache("productoAprobacionList");
        if (cache != null) {
            List<ProductoCompletoAprobacionDTO> lista = cache.get("ALL", List.class);

            // 👇 mapeás al tipo correcto
            ProductoCompletoAprobacionDTO dto = ProductoAprobarMapper.toCompletoDTO(nuevoProducto);

            if (lista != null) {
                List<ProductoCompletoAprobacionDTO> nuevaLista = new ArrayList<>(lista);
                nuevaLista.removeIf(p -> p.getProducto().getId().equals(dto.getProducto().getId()));
                nuevaLista.add(dto);
                cache.put("ALL", nuevaLista);
            } else {
                cache.put("ALL", List.of(dto));
            }
        }
    }



    // Cache por usuario
    public void agregarProductoAprobarAlCache(int usuarioId, ProductoCompletoAprobacionDTO nuevoProducto) {
        Cache cache = cacheManager.getCache("productoIdAprobar");
        if (cache != null) {
            List<ProductoCompletoAprobacionDTO> lista = cache.get(usuarioId, List.class);
            if (lista != null) {
                List<ProductoCompletoAprobacionDTO> nuevaLista = lista.stream()
                        .map(p -> p.getProducto().getId().equals(nuevoProducto.getProducto().getId()) ? nuevoProducto : p)
                        .collect(Collectors.toList());
                // Si no existía, agregarlo
                if (nuevaLista.stream().noneMatch(p -> p.getProducto().getId().equals(nuevoProducto.getProducto().getId()))) {
                    nuevaLista.add(nuevoProducto);
                }
                cache.put(usuarioId, nuevaLista);
            } else {
                cache.put(usuarioId, List.of(nuevoProducto));
            }
        }
    }

    // Cache por producto
    public void agregarProductoCompletoAlCache(int productoId, ProductoCompletoAprobacionDTO productoDTO) {
        Cache cache = cacheManager.getCache("VerProductoCompletoId");
        if (cache != null) {
            List<ProductoCompletoAprobacionDTO> lista = cache.get(productoId, List.class);
            if (lista != null) {
                List<ProductoCompletoAprobacionDTO> nuevaLista = lista.stream()
                        .map(p -> p.getProducto().getId().equals(productoDTO.getProducto().getId()) ? productoDTO : p)
                        .collect(Collectors.toList());
                // Si no existía, agregarlo
                if (nuevaLista.stream().noneMatch(p -> p.getProducto().getId().equals(productoDTO.getProducto().getId()))) {
                    nuevaLista.add(productoDTO);
                }
                cache.put(productoId, nuevaLista);
            } else {
                cache.put(productoId, List.of(productoDTO));
            }
        }
    }

    // Opcional: refrescar toda la cache
    public void refrescarCacheProducto(int productoId) {
        Cache cache = cacheManager.getCache("VerProductoCompletoId");
        if (cache != null) cache.evict(productoId);
    }

    public void refrescarTodosLosProductos() {
        Cache cache = cacheManager.getCache("productoIdAprobar");
        if (cache != null) cache.clear();
    }
    // Cache por lista general
    public void eliminarDeCacheGeneral(Integer productoId) {
        Cache cache = cacheManager.getCache("productoAprobacionList");
        if (cache != null) {
            List<ProductoAprobacionEntity> lista = cache.get("ALL", List.class);
            if (lista != null) {
                List<ProductoAprobacionEntity> nuevaLista = lista.stream()
                        .filter(p -> !p.getId().equals(productoId))
                        .collect(Collectors.toList());
                cache.put("ALL", nuevaLista);
            }
        }
    }

    // Cache por usuario
    public void eliminarDeCacheUsuario(int usuarioId, Integer productoId) {
        System.out.println("[CACHE] Intentando eliminar producto " + productoId + " del usuario " + usuarioId);

        Cache cache = cacheManager.getCache("productoIdAprobar");
        if (cache == null) return;

        Object value = cache.get(usuarioId, Object.class);
        List<ProductoCompletoAprobacionDTO> lista;

        if (!(value instanceof List<?> existingList)) {
            System.out.println("[CACHE] No existe lista en cache para usuario " + usuarioId + ", cargando desde BD...");
            List<ProductoAprobacionEntity> productosUsuario = productoAprobacionRepository.findByUsuarioId_Id(usuarioId);
            lista = productosUsuario.stream()
                    .map(prod -> {
                        try {
                            return productoColaboradorService.obtenerProductoCompletoSinCache(prod.getId());
                        } catch (Exception e) {
                            System.err.println("[CACHE] Error obteniendo producto " + prod.getId() + ": " + e.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(ArrayList::new));
            System.out.println("[CACHE] Lista cargada desde BD para usuario " + usuarioId + ": " + lista);
        } else {
            lista = existingList.stream()
                    .map(p -> (ProductoCompletoAprobacionDTO) p)
                    .collect(Collectors.toCollection(ArrayList::new));
            System.out.println("[CACHE] Lista actual en cache para usuario " + usuarioId + ": " + lista);
        }

        List<ProductoCompletoAprobacionDTO> nuevaLista = lista.stream()
                .filter(p -> !p.getProducto().getId().equals(productoId))
                .collect(Collectors.toCollection(ArrayList::new));

        System.out.println("[CACHE] Lista nueva después de eliminar producto " + productoId + ": " + nuevaLista);

        cache.put(usuarioId, nuevaLista);
        System.out.println("[CACHE] Producto " + productoId + " eliminado del cache del usuario " + usuarioId);
    }



    // Elimina un producto de todas las listas cacheadas
    public void eliminarDeCacheUsuarioEnTodasLasListas(Integer productoId) {
        System.out.println("[CACHE] Eliminando producto " + productoId + " de todas las listas de usuarios");

        Cache cache = cacheManager.getCache("productoIdAprobar");
        if (cache == null) {
            System.out.println("[CACHE] Cache 'productoIdAprobar' no existe");
            return;
        }

        Object nativeCache = cache.getNativeCache();
        if (!(nativeCache instanceof ConcurrentMap<?, ?> map)) {
            System.out.println("[CACHE] El nativeCache no es un ConcurrentMap");
            return;
        }

        if (map.isEmpty()) {
            System.out.println("[CACHE] No hay usuarios en cache");
        }

        for (Object keyObj : map.keySet()) {
            if (!(keyObj instanceof Integer usuarioId)) {
                System.out.println("[CACHE] Clave de cache inesperada: " + keyObj);
                continue;
            }
            System.out.println("[CACHE] Procesando usuarioId=" + usuarioId);
            eliminarDeCacheUsuario(usuarioId, productoId); // usamos el método seguro
        }

        System.out.println("[CACHE] Eliminación del producto " + productoId + " completada en todas las listas");
    }



    // Cache por producto (eliminar directo la clave del producto)
    public void eliminarDeCachePorProducto(int productoId) {
        Cache cache = cacheManager.getCache("VerProductoCompletoId");
        if (cache != null) {
            cache.evict(productoId);
        }
    }


}
