package com.formaprogramada.ecommerce_backend.Web;

import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.Pedido;
import com.formaprogramada.ecommerce_backend.Domain.Service.MercadoPago.MercadoPagoService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
public class MercadoPagoController {

    public MercadoPagoService mercadoPagoService;

    @Value("${mercadopago.access-token}")
    private String mercadolibreToken;

    @RequestMapping(value="api/mp/confirmarPedido", method= RequestMethod.PUT)
    public String confirmarPedido(@RequestBody Pedido pedido, int quantity){
        if(pedido == null){return "error";}
        String title = pedido.getId() + "-" + pedido.getFechaPedido();
        double priceN = pedido.getTotal();
        BigDecimal price= new BigDecimal(priceN);
        String id= pedido.getId().toString();


        return mercadoPagoService.confirmarPedido(mercadolibreToken,title,price,id,quantity);



    }
}
