package com.formaprogramada.ecommerce_backend.Domain.Service.Descuento;

import com.formaprogramada.ecommerce_backend.Domain.Model.Descuento.Descuento;
import org.springframework.stereotype.Service;


public interface DescuentoService {
    Descuento CrearDescuento(Descuento descuento);
}
