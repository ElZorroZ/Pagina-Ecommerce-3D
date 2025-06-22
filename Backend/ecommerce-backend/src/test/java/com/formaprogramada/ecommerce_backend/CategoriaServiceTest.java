package com.formaprogramada.ecommerce_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formaprogramada.ecommerce_backend.Domain.Repository.CategoriaRepository;
import com.formaprogramada.ecommerce_backend.Domain.Service.Impl.CategoriaServiceImpl;
import com.formaprogramada.ecommerce_backend.Domain.Service.TokenVerificacionService;
import com.formaprogramada.ecommerce_backend.Domain.Service.UsuarioService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.CategoriaCreacionRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.UsuarioRegistroRequest;
import com.formaprogramada.ecommerce_backend.Security.SecurityConfig.JWT.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CategoriaServiceTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoriaRepository categoriaRepository;

    @Autowired
    private CategoriaServiceImpl categoriaService;

    @WithMockUser(username = "thiago2007crackz@gmail.com", roles = {"ADMIN"})
    @Test
    void crearUnaCategoria() throws Exception {
        CategoriaCreacionRequest request = CategoriaCreacionRequest.builder()
                .nombre("Categoria1")
                .descripcion("hOLA GOLA que tal2")
                .build();

        mockMvc.perform(post("/api/categoria/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("categoria creada correctamente")));
    }

}
