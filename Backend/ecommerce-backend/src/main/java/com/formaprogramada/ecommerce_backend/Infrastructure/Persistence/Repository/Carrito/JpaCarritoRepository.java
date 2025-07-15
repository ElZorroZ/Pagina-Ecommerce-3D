package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Carrito;

import com.formaprogramada.ecommerce_backend.Domain.Model.Descuento.Carrito;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Carrito.CarritoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface JpaCarritoRepository extends JpaRepository<CarritoEntity, Integer> {
    @Modifying
    @Transactional
    @Query("UPDATE CarritoEntity c SET c.cantidad = c.cantidad + :cantidad, c.precioTotal= c.precioTotal + c.precioUnitario WHERE c.id = :id")
    void sumarCantidad(@Param("id") int id, @Param("cantidad") int cantidad);

    @Modifying
    @Transactional
    @Query("DELETE FROM CarritoEntity WHERE usuarioId=:id")
    void vaciarCarrito(@Param("id") int id);

    @Query("SELECT c FROM CarritoEntity c WHERE c.usuarioId = :id")
    List<CarritoEntity> seleccionarCarrito(@Param("id") int id);
}
