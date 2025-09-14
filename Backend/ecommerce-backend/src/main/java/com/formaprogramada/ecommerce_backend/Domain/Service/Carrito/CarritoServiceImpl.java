package com.formaprogramada.ecommerce_backend.Domain.Service.Carrito;

import com.formaprogramada.ecommerce_backend.Domain.Model.Carrito.Carrito;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Carrito.CarritoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Carrito.CarritoCompletoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Carrito.CarritoProductoDTO;
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
import java.util.Objects;
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
            List<CarritoProductoDTO> carrito = cache.get(usuarioId, List.class);
            if (carrito != null) {
                CarritoProductoDTO nuevoDTO = carritoMapper.toCarritoProductoDTO(nuevoItem);

                // Remover si ya existe el mismo producto con el mismo color
                carrito.removeIf(e -> e.getIdProducto() == nuevoDTO.getIdProducto()
                        && Objects.equals(e.getColorId(), nuevoDTO.getColorId()));

                carrito.add(nuevoDTO);
                cache.put(usuarioId, carrito);
            }
        }

        // Actualizar cache completo
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
                    Cache.ValueWrapper wrapper = cache.get(usuarioId);
                    if (wrapper != null) {
                        List<CarritoProductoDTO> lista = (List<CarritoProductoDTO>) wrapper.get();
                        for (CarritoProductoDTO item : lista) {
                            if (item.getIdProducto() == actualizadoCarrito.getProductoId()) { // usar productoId real
                                item.setCantidad(actualizadoCarrito.getCantidad());
                                break;
                            }
                        }
                        cache.put(usuarioId, lista); // reescribimos la lista completa en la cache
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
        System.out.println("⚠️ Iniciando BorrarProductoCarrito, id=" + id);

        Optional<CarritoEntity> carritoOpt = jpaCarritoRepository.findById(id);
        if (carritoOpt.isEmpty()) {
            System.out.println("❌ No existe el producto en carrito con id=" + id);
            return false;
        }

        CarritoEntity carritoAEliminar = carritoOpt.get();
        Integer usuarioId = carritoAEliminar.getUsuarioId();
        System.out.println("🟢 Producto encontrado. usuarioId=" + usuarioId + ", productoId=" + carritoAEliminar.getProductoId());

        boolean eliminado = jpaCarritoRepository.borrarProductoCarrito(id) > 0;
        System.out.println("🗑 Intento de eliminación completado, eliminado=" + eliminado);

        if (eliminado) {
            System.out.println("♻️ Actualizando cache 'carrito' para usuarioId=" + usuarioId);
            Cache cacheCarrito = cacheManager.getCache("carrito");
            if (cacheCarrito != null) {
                @SuppressWarnings("unchecked")
                List<Object> listaObj = (List<Object>) cacheCarrito.get(usuarioId, List.class);
                System.out.println("🔹 Cache antes de filtrar: " + listaObj);

                if (listaObj != null) {
                    List<CarritoEntity> lista = listaObj.stream()
                            .filter(c -> c instanceof CarritoEntity)
                            .map(c -> (CarritoEntity) c)
                            .collect(Collectors.toList());

                    lista.removeIf(c -> c.getId().equals(id));
                    cacheCarrito.put(usuarioId, lista);
                    System.out.println("🔹 Cache después de eliminar: " + lista);
                }
            }

            System.out.println("♻️ Actualizando cache 'carritoCompleto' para usuarioId=" + usuarioId);
            Cache cacheCompleto = cacheManager.getCache("carritoCompleto");
            if (cacheCompleto != null) {
                List<CarritoCompletoDTO> listaCompleta = jpaCarritoRepository.obtenerCarritoCompletoPorUsuario(usuarioId);
                cacheCompleto.put(usuarioId, listaCompleta);
                System.out.println("🔹 Cache 'carritoCompleto' actualizado con " + listaCompleta.size() + " elementos");
            }
        }

        System.out.println("✅ BorrarProductoCarrito finalizado para id=" + id);
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
    public List<CarritoProductoDTO> LeerUnCarrito(int usuarioId) {
        try {
            return jpaCarritoRepository.seleccionarCarritoDTO(usuarioId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Cacheable(value = "carritoCompleto", key = "#usuarioId")
    @Transactional
    @Override
    public List<CarritoCompletoDTO> LeerUnCarritoCompleto(Integer usuarioId) {
        System.out.println("⚠️ Consultando base de datos: LeerUnCarritoCompleto");

        List<Object[]> resultados = entityManager
                .createNativeQuery("CALL ObtenerCarritoCompletoPorUsuario(:usuarioId)")
                .setParameter("usuarioId", usuarioId)
                .getResultList();

        return resultados.stream().map(r -> new CarritoCompletoDTO(
                (Integer) r[0],                // id
                (Integer) r[1],                // productoId
                (String) r[2],                 // nombre
                (Integer) r[3],                // usuarioId
                ((Number) r[4]).intValue(),    // cantidad
                ((Number) r[5]).doubleValue(), // precioTotal
                ((Number) r[6]).doubleValue(), // precioUnitario
                (Integer) r[7],                // colorId
                r[8],                           // esDigital
                (String) r[9],                 // linkArchivo
                (String) r[10]                 // colorNombre
        )).collect(Collectors.toList());
    }
}
