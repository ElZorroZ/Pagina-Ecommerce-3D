package com.formaprogramada.ecommerce_backend;

import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoArchivoService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.ProductoService;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoArchivoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoEntity;
import com.formaprogramada.ecommerce_backend.Security.SecurityConfig.JWT.JwtService;
import com.formaprogramada.ecommerce_backend.Security.SecurityConfig.SecurityConfig;
import com.formaprogramada.ecommerce_backend.Web.ProductoController;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
@SpringBootTest(classes = {com.formaprogramada.ecommerce_backend.EcommerceBackendApplication.class, com.formaprogramada.ecommerce_backend.TestSecurityConfig.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc

public class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoService productoService;

    @MockBean
    private ProductoArchivoService archivoService;

    @MockBean
    private JwtService jwtService;

    CategoriaEntity categoria = new CategoriaEntity();
}
