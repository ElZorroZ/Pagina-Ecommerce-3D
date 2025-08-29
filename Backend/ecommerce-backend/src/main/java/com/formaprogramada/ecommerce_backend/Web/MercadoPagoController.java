package com.formaprogramada.ecommerce_backend.Web;

import com.mercadopago.MercadoPagoConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MercadoPagoController {

    @Value("${codigo.mercadoLibre}")
    private String mercadolibreToken;

    @RequestMapping(value="api/mp", method= RequestMethod.PUT)
    public String confirmarPedido(){
        try{
            MercadoPagoConfig.setAccessToken(mercadolibreToken);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
        return "prueba";
    }
}
