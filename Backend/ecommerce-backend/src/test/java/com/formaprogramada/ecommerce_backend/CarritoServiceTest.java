package com.formaprogramada.ecommerce_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Carrito.CarritoRepository;
import com.formaprogramada.ecommerce_backend.Domain.Service.Carrito.CarritoService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Carrito.CarritoAgregarRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioUpdatePedido;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Carrito.CarritoRepositoryImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc

public class CarritoServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private CarritoRepositoryImpl descuentoRepositoryimpl;


    @WithMockUser(username = "thiago2007crackz@gmail.com", roles = {"ADMIN"})
    @Test
    void testAgregarCarrito() throws Exception {

        CarritoAgregarRequest request = new CarritoAgregarRequest();
        request.setProductoId(2);
        request.setUsuarioId(1);
        request.setCantidad(1);
        request.setPrecioTotal(100);
        request.setPrecioUnitario(20);
        request.setEsDigital(true);



        mockMvc.perform(post("/api/carrito/agregarProductoaCarrito")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @WithMockUser(username = "thiago2007crackz@gmail.com", roles = {"ADMIN"})
    @Test
    void testSumarCarrito() throws Exception {

        mockMvc.perform(put("/api/carrito/sumarCantidad/1/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @WithMockUser(username = "thiago2007crackz@gmail.com", roles = {"ADMIN"})
    @Test
    void testBorrarProductoDeCarrito() throws Exception {

        mockMvc.perform(delete("/api/carrito/borrarProductoaCarrito/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @WithMockUser(username = "thiago2007crackz@gmail.com", roles = {"ADMIN"})
    @Test
    void testBorrarCarrito() throws Exception {

        mockMvc.perform(delete("/api/carrito/vaciarCarrito/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    @WithMockUser(username = "thiago2007crackz@gmail.com", roles = {"ADMIN"})
    @Test
    void testObtenerCarrito() throws Exception {

        mockMvc.perform(get("/api/carrito/verCarrito/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @WithMockUser(username = "thiago2007crackz@gmail.com", roles = {"ADMIN"})
    @Test
    void testActualizarUsuario() throws Exception {
        UsuarioUpdatePedido request= new UsuarioUpdatePedido();
        request.setCp("4245");
        request.setApellido("VelazquesMejorado");
        request.setCiudad("Guadalajara");
        request.setNombre("Zorro");
        request.setGmail("thiago2007crackz@gmail.com");
        request.setDireccion("por ahi");
        request.setTelefono("18384135");

        mockMvc.perform(put("/api/auth/actualizar-usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }


}
