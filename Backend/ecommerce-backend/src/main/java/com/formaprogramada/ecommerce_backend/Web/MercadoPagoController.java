package com.formaprogramada.ecommerce_backend.Web;

import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.Pedido;
import com.formaprogramada.ecommerce_backend.Domain.Service.MercadoPago.MercadoPagoService;

import com.formaprogramada.ecommerce_backend.Domain.Service.Pedido.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
@RestController
@RequestMapping("/api/mp")
public class MercadoPagoController {

    private final MercadoPagoService mercadoPagoService;

    @Autowired
    private PedidoService pedidoService;

    @Value("${mercadopago.access-token}")
    private String mercadolibreToken;

    public MercadoPagoController(MercadoPagoService mercadoPagoService) {
        this.mercadoPagoService = mercadoPagoService;
    }

    // Crear preferencia y devolver init_point
    @PutMapping("/confirmarPedido")
    public ResponseEntity<Map<String, String>> confirmarPedido(@RequestBody Pedido pedido, @RequestParam int quantity) {
        if (pedido == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Pedido inválido"));
        }

        String title = pedido.getId() + "-" + pedido.getFechaPedido();
        BigDecimal price = BigDecimal.valueOf(pedido.getTotal());
        String initPoint = mercadoPagoService.confirmarPedido(mercadolibreToken, title, price, pedido.getId().toString(), quantity);

        return ResponseEntity.ok(Map.of("initPoint", initPoint));
    }

    // URLs para redirección después del pago
    @GetMapping("/pago-exitoso")
    public String pagoExitoso(@RequestParam int pedidoId) {
        pedidoService.CambiarEstado("PAGADO", pedidoId);
        return "redirect:/gracias-por-tu-compra";
    }

    @GetMapping("/pago-pendiente")
    public String pagoPendiente(@RequestParam int pedidoId) {
        pedidoService.CambiarEstado("PENDIENTE", pedidoId);
        return "redirect:/estado-pendiente";
    }

    @GetMapping("/pago-fallido")
    public String pagoFallido(@RequestParam int pedidoId) {
        pedidoService.CambiarEstado("FALLIDO", pedidoId);
        return "redirect:/pago-fallido";
    }

    // Webhook para notificaciones automáticas de Mercado Pago
    @PostMapping("/webhook")
    public ResponseEntity<Void> mpWebhook(@RequestBody Map<String, Object> payload) {
        try {
            String type = (String) payload.get("type");
            if ("payment".equals(type)) {
                Map<String, Object> data = (Map<String, Object>) payload.get("data");

                // ID del pago en Mercado Pago como String
                String mercadoPagoId = String.valueOf(data.get("id"));

                // Buscamos el pedido usando mercadoPagoId
                Pedido pedido = pedidoService.obtenerPedidoPorMercadoPagoId(mercadoPagoId);
                if (pedido != null) {
                    Map<String, Object> payment = (Map<String, Object>) data.get("object");
                    String status = (String) payment.get("status"); // approved, pending, rejected, etc.
                    pedidoService.CambiarEstado(status.toUpperCase(), pedido.getId());
                }
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
