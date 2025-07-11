package com.formaprogramada.ecommerce_backend.Domain.Service.Descuento;

import com.formaprogramada.ecommerce_backend.Domain.Model.Descuento.Carrito;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Carrito.CarritoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Carrito.CarritoEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CarritoServiceImpl implements CarritoService {

    private CarritoRepository carritoRepository;

    @Override
    public Carrito AgregarCarrito(Carrito carrito) {
        return carritoRepository.CrearDescuento(carrito);
    }

    @Override
    public Boolean SumarCantidad(int cantidad, int id) {
        return carritoRepository.SumarCantidad(cantidad, id);
    }

    @Override
    public Boolean BorrarProductoCarrito(int id) {
        return carritoRepository.BorrarProductoCarrito(id);
    }

    @Override
    public Boolean VaciarCarrito(int id) {
        return carritoRepository.VaciarCarrito(id);
    }

    @Override
    public List<CarritoEntity> LeerUnCarrito(int id) {

        return carritoRepository.LeerUnCarrito(id);
    }

}
