package com.formaprogramada.ecommerce_backend.Web;
import com.formaprogramada.ecommerce_backend.Domain.Model.Descuento.Carrito;
import com.formaprogramada.ecommerce_backend.Domain.Service.Descuento.CarritoService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Descuento.CarritoAgregarRequest;
import com.formaprogramada.ecommerce_backend.Mapper.Descuento.CarritoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;


    @PostMapping("/agregarProductoaCarrito")
    public ResponseEntity<List<Carrito>> agregarProductoaCarrito(@Valid @RequestBody CarritoAgregarRequest carritoAgregarRequest) {
        try{

            var carrito= CarritoMapper.toDomain(carritoAgregarRequest);
            carrito= carritoService.AgregarCarrito(carrito);
            List<Carrito> lista=new ArrayList<Carrito>();
            lista.add(carrito);
            System.out.println(lista);
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
