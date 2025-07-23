package com.formaprogramada.ecommerce_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoAprobadoCacheService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoAprobadoService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoArchivoService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Categoria.CategoriaCrearRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobacionRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobacionResponse;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;
import com.formaprogramada.ecommerce_backend.Security.SecurityConfig.JWT.JwtService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {com.formaprogramada.ecommerce_backend.EcommerceBackendApplication.class, com.formaprogramada.ecommerce_backend.TestSecurityConfig.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ProductoAprobadoController {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductoAprobadoService productoService;
    @MockBean
    private ProductoAprobadoCacheService productoAprobadoCacheService;
    @MockBean
    private ProductoArchivoService archivoService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @WithMockUser(username = "thiago2007crackz@gmail.com", roles = {"ADMIN"})
    @Test
    void testCrearUnProductoPorAprobarSinArchivo() throws Exception {

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(1);

        ProductoAprobacionRequest request = new ProductoAprobacionRequest();
        request.setNombre("Hola");
        request.setDescripcion("Hola");
        request.setCreadorId(usuario);
        request.setCategoriaId(1);
        request.setCodigoInicial("13ef");
        request.setPrecio(900);
        request.setDimensionAlto(93);
        request.setDimensionAncho(92);
        request.setDimensionProfundidad(2);
        request.setMaterial("Madera");
        request.setTecnica("Filar");
        request.setPeso("1kg");
        request.setColores(Collections.singletonList("rojo"));

        // Solo el JSON del DTO como multipart
        MockMultipartFile productoJson = new MockMultipartFile(
                "producto",                        // nombre del campo
                "producto.json",                   // filename (aunque sea genérico)
                MediaType.APPLICATION_JSON_VALUE,  // tipo de contenido
                objectMapper.writeValueAsBytes(request) // contenido serializado
        );

        mockMvc.perform(multipart("/api/productosAprobacion/crearAprobacionProducto")
                        .file(productoJson)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }

}
