package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Carrito;

import com.formaprogramada.ecommerce_backend.Domain.Model.Carrito.Carrito;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Carrito.CarritoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Carrito.CarritoEntity;
import com.formaprogramada.ecommerce_backend.Mapper.Carrito.CarritoMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@AllArgsConstructor
public class CarritoRepositoryImpl implements CarritoRepository {
    private CarritoMapper carritoMapper;
    private JpaCarritoRepository jpaCarritoRepository;


    @Override
    public Boolean SumarCantidad(int cantidad, int id) {
        try {
            jpaCarritoRepository.sumarCantidad(id, cantidad);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean BorrarProductoCarrito(int id) {
        try{
            jpaCarritoRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean VaciarCarrito(int id) {
        try{
            jpaCarritoRepository.vaciarCarrito(id);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<CarritoEntity> LeerUnCarrito(int id) {
        try{
            List<CarritoEntity> lista= jpaCarritoRepository.seleccionarCarrito(id);
            return lista;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
