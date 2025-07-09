package com.formaprogramada.ecommerce_backend.Domain.Service.Descuento;

import com.formaprogramada.ecommerce_backend.Domain.Model.Descuento.Descuento;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Descuento.DescuentoRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DescuentoServiceImpl implements DescuentoService{

    private DescuentoRepository descuentoRepository;

    @Override
    public Descuento CrearDescuento(Descuento descuento) {

        return descuentoRepository.CrearDescuento(descuento);
    }
}
