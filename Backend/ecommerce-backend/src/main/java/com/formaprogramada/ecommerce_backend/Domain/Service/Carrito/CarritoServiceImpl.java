package com.formaprogramada.ecommerce_backend.Domain.Service.Carrito;

import com.formaprogramada.ecommerce_backend.Domain.Model.Carrito.Carrito;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Carrito.CarritoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Carrito.CarritoCompletoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Carrito.CarritoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Carrito.JpaCarritoRepository;
import com.formaprogramada.ecommerce_backend.Mapper.Carrito.CarritoMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CarritoServiceImpl implements CarritoService {

    private CarritoRepository carritoRepository;
    private JpaCarritoRepository jpaCarritoRepository;
    private CarritoMapper carritoMapper;
    @Autowired
    private CacheManager cacheManager;
    @PersistenceContext
    private EntityManager entityManager;

    private void actualizarCacheCarrito(int usuarioId, CarritoEntity nuevoItem) {
        Cache cache = cacheManager.getCache("carrito");
        if (cache != null) {
            List<CarritoEntity> carrito = cache.get(usuarioId, List.class);
            if (carrito != null) {
                // Remover si ya existe
                carrito.removeIf(e ->
                        e.getProductoId() == nuevoItem.getProductoId()
                                && e.isEsDigital() == nuevoItem.isEsDigital()
                );

                // Agregar actualizado
                carrito.add(nuevoItem);

                cache.put(usuarioId, carrito);
            }
        }

        // 🔄 También actualizar el cache del carrito completo (DTO)
        Cache cacheCompleto = cacheManager.getCache("carritoCompleto");
        if (cacheCompleto != null) {
            List<CarritoCompletoDTO> listaActualizada = jpaCarritoRepository.obtenerCarritoCompletoPorUsuario(usuarioId);
            cacheCompleto.put(usuarioId, listaActualizada);

        }
    }


    @Override
    public Carrito AgregarCarrito(Carrito carrito) {
        System.out.println("⚠️ Consultando base de datos: AgregarProductoCarrito");
        boolean existeOtroFormato = jpaCarritoRepository.existsByUsuarioIdAndProductoIdAndEsDigital(
                carrito.getUsuarioId(),
                carrito.getProductoId(),
                !carrito.isEsDigital()
        );

        if (existeOtroFormato) {
            throw new IllegalArgumentException("No puede agregar el mismo producto en formato digital y físico al carrito.");
        }

        Optional<CarritoEntity> carritoEntityOpt = jpaCarritoRepository.findByUsuarioIdAndProductoIdAndEsDigital(
                carrito.getUsuarioId(),
                carrito.getProductoId(),
                carrito.isEsDigital()
        );

        CarritoEntity savedEntity;

        if (carritoEntityOpt.isPresent()) {
            CarritoEntity carritoEntity = carritoEntityOpt.get();
            Carrito existente = carritoMapper.toDomain2(carritoEntity);

            if (carrito.isEsDigital()) {
                throw new IllegalArgumentException("El producto digital ya está en el carrito. No puede agregar más unidades.");
            }

            existente.setCantidad(existente.getCantidad() + carrito.getCantidad());
            existente.setPrecioTotal(existente.getPrecioUnitario() * existente.getCantidad());

            CarritoEntity actualizadoEntity = carritoMapper.toEntity(existente);
            savedEntity = jpaCarritoRepository.save(actualizadoEntity);
        } else {
            if (carrito.isEsDigital() && carrito.getCantidad() > 1) {
                throw new IllegalArgumentException("La compra digital no puede tener cantidad mayor a 1");
            }

            CarritoEntity entityNuevo = carritoMapper.toEntity(carrito);
            savedEntity = jpaCarritoRepository.save(entityNuevo);
        }

        // 🔄 Actualizar cache
        actualizarCacheCarrito(carrito.getUsuarioId(), savedEntity);

        return carritoMapper.toDomain2(savedEntity);
    }

    @Transactional
    @Override
    public Boolean SumarCantidad(int cantidad, int carritoId) {
        try {
            System.out.println("⚠️ Consultando base de datos: SumarCantidadProductoCarrito");
            int filasAfectadas = jpaCarritoRepository.sumarCantidad(carritoId, cantidad);
            System.out.println("Filas afectadas: " + filasAfectadas);

            if (filasAfectadas <= 0) {
                return false;
            }

            Optional<CarritoEntity> carritoEntityOpt = jpaCarritoRepository.findById(carritoId);
            if (carritoEntityOpt.isPresent()) {
                CarritoEntity actualizadoCarrito = carritoEntityOpt.get();
                entityManager.refresh(actualizadoCarrito);
                System.out.println("Cantidad actualizada: " + actualizadoCarrito.getCantidad());
                System.out.println("Precio total actualizado: " + actualizadoCarrito.getPrecioTotal());
                Integer usuarioId = actualizadoCarrito.getUsuarioId();

                Cache cache = cacheManager.getCache("carrito");
                if (cache != null) {
                    List<CarritoEntity> lista = cache.get(usuarioId, List.class);
                    if (lista != null) {
                        for (CarritoEntity item : lista) {
                            if (item.getId() == carritoId) {
                                item.setCantidad(item.getCantidad() + cantidad);
                                item.setPrecioTotal(item.getCantidad() * item.getPrecioUnitario());
                                break;
                            }
                        }
                        cache.put(usuarioId, lista);
                    }
                }

                Cache cacheCompleto = cacheManager.getCache("carritoCompleto");
                if (cacheCompleto != null) {
                    List<CarritoCompletoDTO> listaActualizada = jpaCarritoRepository.obtenerCarritoCompletoPorUsuario(usuarioId);
                    cacheCompleto.put(usuarioId, listaActualizada);
                }
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }




    @Override
    public Boolean BorrarProductoCarrito(int id) {
        System.out.println("⚠️ Consultando base de datos: BorrarProductoCarrito");

        Optional<CarritoEntity> carritoOpt = jpaCarritoRepository.findById(id);
        if (carritoOpt.isEmpty()) {
            // No existe el producto en carrito
            return false;
        }

        CarritoEntity carritoAEliminar = carritoOpt.get();
        Integer usuarioId = carritoAEliminar.getUsuarioId();

        boolean eliminado = jpaCarritoRepository.borrarProductoCarrito(id) > 0;

        if (eliminado) {
            Cache cacheCarrito = cacheManager.getCache("carrito");
            if (cacheCarrito != null) {
                List<CarritoEntity> lista = cacheCarrito.get(usuarioId, List.class);
                if (lista != null) {
                    lista.removeIf(c -> c.getId() == id);
                    cacheCarrito.put(usuarioId, lista);
                }
            }

            Cache cacheCompleto = cacheManager.getCache("carritoCompleto");
            if (cacheCompleto != null) {
                List<CarritoCompletoDTO> listaCompleta = jpaCarritoRepository.obtenerCarritoCompletoPorUsuario(usuarioId);
                cacheCompleto.put(usuarioId, listaCompleta);
            }
        }

        return eliminado;
    }




    @Override
    public Boolean VaciarCarrito(int usuarioId) {
        System.out.println("⚠️ Consultando base de datos: VaciarCarrito");
        boolean vaciado = carritoRepository.VaciarCarrito(usuarioId);

        if (vaciado) {
            // Evitar cache carrito simple
            Cache cacheCarrito = cacheManager.getCache("carrito");
            if (cacheCarrito != null) {
                cacheCarrito.evict(usuarioId);
            }

            // Evitar cache carrito completo
            Cache cacheCompleto = cacheManager.getCache("carritoCompleto");
            if (cacheCompleto != null) {
                cacheCompleto.evict(usuarioId);
            }
        }

        return vaciado;
    }


    @Cacheable(value = "carrito", key = "#id")
    @Override
    public List<CarritoEntity> LeerUnCarrito(int id) {
        System.out.println("⚠️ Consultando base de datos: LeerUnCarrito");
        return carritoRepository.LeerUnCarrito(id);
    }
    @Cacheable(value = "carritoCompleto", key = "#usuarioId")
    @Override
    public List<CarritoCompletoDTO> LeerUnCarritoCompleto(Integer usuarioId) {
        System.out.println("⚠️ Consultando base de datos: LeerUnCarritoCompleto");
        return jpaCarritoRepository.obtenerCarritoCompletoPorUsuario(usuarioId);
    }
}
