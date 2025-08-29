package com.formaprogramada.ecommerce_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.Pedido;
import com.formaprogramada.ecommerce_backend.Domain.Service.Pedido.PedidoService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Carrito.CarritoAgregarRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Carrito.CarritoEntity;
import com.formaprogramada.ecommerce_backend.Mapper.Pedido.PedidoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {com.formaprogramada.ecommerce_backend.EcommerceBackendApplication.class, com.formaprogramada.ecommerce_backend.TestSecurityConfig.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class PedidoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PedidoService pedidoService;

    @MockBean
    private PedidoMapper pedidoMapper; // solo si PedidoMapper no es static

    @Autowired
    private ObjectMapper objectMapper;

    @WithMockUser(username = "thiago2007crackz@gmail.com", roles = {"ADMIN"})
    @Test
    void testAgregarPedido() throws Exception {

            CarritoEntity carrito1= new CarritoEntity();
            CarritoEntity carrito2 = new CarritoEntity();
            CarritoEntity carrito3 = new CarritoEntity();
            CarritoEntity carrito4 = new CarritoEntity();

            carrito1.setCantidad(2);
            carrito1.setEsDigital(false);
            carrito1.setProductoId(2);
            carrito1.setPrecioUnitario(10);
            carrito1.setPrecioTotal(20);
            carrito1.setUsuarioId(1);

            carrito2.setCantidad(2);
            carrito2.setEsDigital(false);
            carrito2.setProductoId(7);
            carrito2.setPrecioUnitario(100);
            carrito2.setPrecioTotal(200);
            carrito2.setUsuarioId(1);

            carrito3.setCantidad(1);
            carrito3.setEsDigital(true);
            carrito3.setProductoId(6);
            carrito3.setPrecioUnitario(100);
            carrito3.setPrecioTotal(100);
            carrito3.setUsuarioId(1);

            carrito4.setCantidad(1);
            carrito4.setEsDigital(false);
            carrito4.setProductoId(21);
            carrito4.setPrecioUnitario(10);
            carrito4.setPrecioTotal(10);
            carrito4.setUsuarioId(1);



            List<CarritoEntity> lista = new ArrayList<>();

            lista.add(carrito1);
        lista.add(carrito2);
        lista.add(carrito3);
        lista.add(carrito4);
        mockMvc.perform(post("/api/pedido/crearPedido")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lista)))
                .andExpect(status().isOk());
    }

    @WithMockUser(username = "thiago2007crackz@gmail.com", roles = {"ADMIN"})
    @Test
    void testBorrarPedido() throws Exception {


        mockMvc.perform(delete("/api/pedido/borrarPedido")
                        .param("id", String.valueOf(9))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

}
