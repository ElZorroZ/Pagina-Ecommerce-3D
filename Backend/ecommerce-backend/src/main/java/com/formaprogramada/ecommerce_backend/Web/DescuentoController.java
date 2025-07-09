package com.formaprogramada.ecommerce_backend.Web;
import com.formaprogramada.ecommerce_backend.Domain.Model.Descuento.Descuento;
import com.formaprogramada.ecommerce_backend.Domain.Service.Descuento.DescuentoService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Descuento.DescuentoCrearRequest;
import com.formaprogramada.ecommerce_backend.Mapper.Descuento.DescuentoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/descuento")
@RequiredArgsConstructor
public class DescuentoController {

    private final DescuentoService descuentoService;


    @PostMapping("/crearDestacado")
    public List<Descuento> crearDescuento(@Valid @RequestBody DescuentoCrearRequest descuentoCrearRequest) {
        try{

            var descuento= DescuentoMapper.toDomain(descuentoCrearRequest);
            descuento=descuentoService.CrearDescuento(descuento);
            List<Descuento> lista=new ArrayList<Descuento>();
            lista.add(descuento);
            System.out.println(lista);
            return lista;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
