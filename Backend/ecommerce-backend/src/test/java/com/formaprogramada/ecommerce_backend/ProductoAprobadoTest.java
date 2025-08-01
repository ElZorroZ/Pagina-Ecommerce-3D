package com.formaprogramada.ecommerce_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoAprobar.ProductoAprobadoCacheService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoAprobar.ProductoAprobadoService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoArchivoService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ProductoAprobacionRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;
import com.formaprogramada.ecommerce_backend.Security.SecurityConfig.JWT.JwtService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {com.formaprogramada.ecommerce_backend.EcommerceBackendApplication.class, com.formaprogramada.ecommerce_backend.TestSecurityConfig.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ProductoAprobadoTest {


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
    void testCrearUnProductoPorAprobar() throws Exception {

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(2);

        ProductoAprobacionRequest request = new ProductoAprobacionRequest();
        request.setNombre("Hola3");
        request.setDescripcion("Hola3");
        request.setCreadorId(usuario);
        request.setCategoriaId(3);
        request.setCodigoInicial("13ef3");
        request.setPrecio(9002);
        request.setDimensionAlto(932);
        request.setDimensionAncho(922);
        request.setDimensionProfundidad(22);
        request.setMaterial("Madera3");
        request.setTecnica("Filar3");
        request.setPeso("1kg3");
        request.setColores(Collections.singletonList("verde"));

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

    @WithMockUser(username = "thiago2007crackz@gmail.com", roles = {"ADMIN"})
    @Test
    void testAprobarUnProducto() throws Exception {

        int id=1;
        String codigoInicial = "0001";
        String versionStr ="1";
        String seguimiento = "?";

        mockMvc.perform(post("/api/productosAprobacion/AprobarProducto")
                        .param("id", "3")
                        .param("codigoInicial", "123")
                        .param("versionStr", "v1")
                        .param("seguimiento", "abc")) // opcional en este caso
            .andExpect(status().isOk());
    }

    @WithMockUser(username = "thiago2007crackz@gmail.com", roles = {"ADMIN"})
    @Test
    void testborrarUnProducto() throws Exception {


        mockMvc.perform(delete("/api/productosAprobacion/BorrarProducto")
                        .param("id", "3"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "thiago2007crackz@gmail.com", roles = {"ADMIN"})
    void testVerProductos_OK() throws Exception {

        mockMvc.perform(get("/api/productosAprobacion/VerProductos") // ajustá si tenés prefijo distinto
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "thiago2007crackz@gmail.com", roles = {"ADMIN"})
    void testVerProductos_OK2() throws Exception {

        mockMvc.perform(get("/api/productosAprobacion/VerProductos_de/1") // ajustá si tenés prefijo distinto
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "thiago2007crackz@gmail.com", roles = {"ADMIN"})
    void testVerProducto_OK() throws Exception {

        mockMvc.perform(get("/api/productosAprobacion/VerProductoCompleto/4") // ajustá si tenés prefijo distinto
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

}
