package com.formaprogramada.ecommerce_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Descuento.DescuentoRepository;
import com.formaprogramada.ecommerce_backend.Domain.Service.Descuento.DescuentoService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Descuento.DescuentoCrearRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Descuento.DescuentoRepositoryImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class DescuentoServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DescuentoRepository descuentoRepository;

    @Autowired
    private DescuentoRepositoryImpl descuentoRepositoryimpl;

    @Autowired
    private DescuentoService descuentoService;

    @WithMockUser(username = "thiago2007crackz@gmail.com", roles = {"ADMIN"})
    @Test
    void testAgregarDescuento() throws Exception {

        DescuentoCrearRequest request = new DescuentoCrearRequest();
        request.setNombre("Des2");
        request.setDescripcion("uNA Descripcion de DESCUenTO!2222343$");
        request.setPorcentaje(20.5);


        mockMvc.perform(post("/api/descuento/crearDestacado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
