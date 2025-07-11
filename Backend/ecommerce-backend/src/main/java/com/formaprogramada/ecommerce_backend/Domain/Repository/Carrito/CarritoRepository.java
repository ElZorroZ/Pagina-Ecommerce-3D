package com.formaprogramada.ecommerce_backend.Domain.Repository.Carrito;

import com.formaprogramada.ecommerce_backend.Domain.Model.Descuento.Carrito;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Carrito.CarritoEntity;

import java.util.List;

public interface CarritoRepository {
    Carrito CrearDescuento(Carrito carrito);
    Boolean SumarCantidad(int cantidad, int id);
    Boolean BorrarProductoCarrito(int id);
    Boolean VaciarCarrito(int id);
    List<CarritoEntity> LeerUnCarrito(int id);

}