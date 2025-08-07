package com.formaprogramada.ecommerce_backend.Domain.Service.Carrito;

import com.formaprogramada.ecommerce_backend.Domain.Model.Carrito.Carrito;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Carrito.CarritoCompletoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Carrito.CarritoEntity;

import java.util.List;


public interface CarritoService {
    Carrito AgregarCarrito(Carrito carrito);
    Boolean SumarCantidad(int cantidad, int id);
    Boolean BorrarProductoCarrito(int id);
    Boolean VaciarCarrito(int id);
    List<CarritoEntity> LeerUnCarrito(int id);
    List<CarritoCompletoDTO> LeerUnCarritoCompleto(Integer usuarioId) ;
}
