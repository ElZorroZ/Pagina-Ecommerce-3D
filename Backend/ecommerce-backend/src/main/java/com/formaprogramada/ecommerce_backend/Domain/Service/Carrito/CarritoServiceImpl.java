package com.formaprogramada.ecommerce_backend.Domain.Service.Carrito;

import com.formaprogramada.ecommerce_backend.Domain.Model.Carrito.Carrito;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Carrito.CarritoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Carrito.CarritoCompletoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Carrito.CarritoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoColorEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Carrito.JpaCarritoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.JpaProductoColorRepository;
import com.formaprogramada.ecommerce_backend.Mapper.Carrito.CarritoMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CarritoServiceImpl implements CarritoService {

    private CarritoRepository carritoRepository;
    private JpaCarritoRepository jpaCarritoRepository;
    private JpaProductoColorRepository productoColorRepository;
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

    @Transactional
    @Override
    public Carrito AgregarCarrito(Carrito carrito) {
        try {
            System.out.println("=== AgregarCarrito llamado ===");
            System.out.println("UsuarioId: " + carrito.getUsuarioId());
            System.out.println("ProductoId: " + carrito.getProductoId());
            System.out.println("ColorId: " + carrito.getColorId());
            System.out.println("EsDigital: " + carrito.getEsDigital());
            System.out.println("Cantidad: " + carrito.getCantidad());

            // Convertir colorId a entidad, si existe
            ProductoColorEntity colorEntity = null;
            if (carrito.getColorId() != null && carrito.getColorId() != 0) {
                colorEntity = productoColorRepository.findById(carrito.getColorId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Color no encontrado"));
            }

            // Verificar si ya existe el producto con el mismo color
            Optional<CarritoEntity> carritoExistenteOpt = jpaCarritoRepository
                    .findByUsuarioIdAndProductoIdAndColor(
                            carrito.getUsuarioId(),
                            carrito.getProductoId(),
                            colorEntity
                    );

            if (carritoExistenteOpt.isPresent()) {
                System.out.println("Producto ya existe en carrito con mismo color. No se permite duplicado.");
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Ya existe este producto en el carrito con el mismo color"
                );
            }

            // No existe → crear nuevo registro
            CarritoEntity nuevoEntity = carritoMapper.toEntity(carrito, productoColorRepository);
            nuevoEntity.setColor(colorEntity);
            CarritoEntity savedEntity = jpaCarritoRepository.save(nuevoEntity);

            System.out.println("Producto agregado al carrito con ID: " + savedEntity.getId() +
                    ", UsuarioId: " + savedEntity.getUsuarioId() +
                    ", ProductoId: " + savedEntity.getProductoId() +
                    ", ColorId: " + (savedEntity.getColor() != null ? savedEntity.getColor().getId() : null));

            // Actualizar cache
            actualizarCacheCarrito(carrito.getUsuarioId(), savedEntity);

            System.out.println("=== AgregarCarrito finalizado ===");
            return carritoMapper.toDomain2(savedEntity);

        } catch (ResponseStatusException e) {
            System.out.println("ResponseStatusException: " + e.getReason());
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", e);
        }
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
                @SuppressWarnings("unchecked")
                List<Object> listaObj = (List<Object>) cacheCarrito.get(usuarioId, List.class);
                if (listaObj != null) {
                    // Crear una lista filtrada solo con CarritoEntity
                    List<CarritoEntity> lista = listaObj.stream()
                            .filter(c -> c instanceof CarritoEntity)
                            .map(c -> (CarritoEntity) c)
                            .collect(Collectors.toList());

                    // Eliminar el que tenga el ID
                    lista.removeIf(c -> c.getId().equals(id));

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


    @Cacheable(value = "carrito", key = "#usuarioId")
    @Transactional
    @Override
    public List<Integer> LeerUnCarrito(int usuarioId) {
        try {
            return jpaCarritoRepository.seleccionarIdsCarrito(usuarioId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Cacheable(value = "carritoCompleto", key = "#usuarioId")
    @Transactional
    @Override
    public List<CarritoCompletoDTO> LeerUnCarritoCompleto(Integer usuarioId) {
        System.out.println("⚠️ Consultando base de datos: LeerUnCarritoCompleto");
        return jpaCarritoRepository.obtenerCarritoCompletoPorUsuario(usuarioId);
    }
}
