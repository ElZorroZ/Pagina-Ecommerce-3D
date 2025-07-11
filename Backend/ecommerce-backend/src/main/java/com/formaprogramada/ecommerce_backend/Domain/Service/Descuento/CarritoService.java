package com.formaprogramada.ecommerce_backend.Domain.Service.Descuento;

import com.formaprogramada.ecommerce_backend.Domain.Model.Descuento.Carrito;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Descuento.DescuentoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Carrito.CarritoEntity;

import java.util.List;


public interface CarritoService {
    Carrito AgregarCarrito(Carrito carrito);
    Boolean SumarCantidad(int cantidad, int id);
    Boolean BorrarProductoCarrito(int id);
    Boolean VaciarCarrito(int id);
    List<CarritoEntity> LeerUnCarrito(int id);

}
