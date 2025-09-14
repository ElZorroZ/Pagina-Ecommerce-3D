package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Carrito;

import com.formaprogramada.ecommerce_backend.Domain.Model.Carrito.Carrito;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Carrito.CarritoCompletoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Carrito.CarritoProductoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Carrito.CarritoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoColorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface JpaCarritoRepository extends JpaRepository<CarritoEntity, Integer> {

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE CarritoEntity c SET c.cantidad = c.cantidad + :cantidad, c.precioTotal = c.precioTotal + c.precioUnitario WHERE c.id = :id")
    int sumarCantidad(@Param("id") int id, @Param("cantidad") int cantidad);

    @Modifying
    @Transactional
    @Query("DELETE FROM CarritoEntity WHERE usuarioId=:id")
    void vaciarCarrito(@Param("id") int id);

    @Modifying
    @Transactional
    @Query("DELETE FROM CarritoEntity c WHERE c.id = :id")
    int borrarProductoCarrito(@Param("id") int id);

    @Query("SELECT c FROM CarritoEntity c WHERE c.usuarioId = :id")
    List<CarritoEntity> seleccionarCarrito(@Param("id") int id);

    boolean existsByUsuarioIdAndProductoIdAndEsDigital(Integer usuarioId, Integer productoId, boolean esDigital);

    Optional<CarritoEntity> findByUsuarioIdAndProductoIdAndEsDigital(Integer usuarioId, Integer productoId, boolean esDigital);

    // Nuevo método para considerar el color
    Optional<CarritoEntity> findByUsuarioIdAndProductoIdAndColor(
            Integer usuarioId,
            Integer productoId,
            ProductoColorEntity color
    );
    @Query("""
    SELECT new com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Carrito.CarritoProductoDTO(
        c.productoId,
        p.nombre,
        c.cantidad,
        c.color.id
    )
    FROM CarritoEntity c
    JOIN ProductoEntity p ON c.productoId = p.id
    WHERE c.usuarioId = :usuarioId
""")
    List<CarritoProductoDTO> seleccionarCarritoDTO(@Param("usuarioId") int usuarioId);


    @Query("SELECT DISTINCT c.usuarioId FROM CarritoEntity c")
    List<Integer> obtenerIdsUsuariosConCarrito();
    @Query("SELECT c.id FROM CarritoEntity c WHERE c.usuarioId = :usuarioId")
    List<Integer> seleccionarIdsCarrito(@Param("usuarioId") int usuarioId);
    @Query(value = "CALL ObtenerCarritoCompletoPorUsuario(:usuarioId)", nativeQuery = true)
    List<CarritoCompletoDTO> obtenerCarritoCompletoPorUsuario(@Param("usuarioId") Integer usuarioId);
}

