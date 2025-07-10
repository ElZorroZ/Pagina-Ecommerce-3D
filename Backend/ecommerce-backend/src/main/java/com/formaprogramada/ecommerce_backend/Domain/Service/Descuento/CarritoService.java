package com.formaprogramada.ecommerce_backend.Domain.Service.Descuento;

import com.formaprogramada.ecommerce_backend.Domain.Model.Descuento.Carrito;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Descuento.DescuentoDTO;

import java.util.List;


public interface CarritoService {
    Carrito AgregarCarrito(Carrito carrito);
}
