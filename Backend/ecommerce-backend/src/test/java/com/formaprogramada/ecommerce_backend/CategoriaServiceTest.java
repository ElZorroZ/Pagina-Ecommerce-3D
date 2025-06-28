package com.formaprogramada.ecommerce_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Categoria.CategoriaCrearRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Categoria.CategoriaUpdateRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc

class CategoriaServiceTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @WithMockUser(username = "thiago2007crackz@gmail.com", roles = {"ADMIN"})
    @Test
    void testAgregarCategoria() throws Exception {

        CategoriaCrearRequest request = new CategoriaCrearRequest();
        request.setNombre("Hola2");
        request.setDescripcion("uNA Descripcion1");



        mockMvc.perform(put("/api/categoria/crearCategoria")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Se hizo bien")));
    }



    @WithMockUser(username = "thiago2007crackz@gmail.com", roles = {"ADMIN"})
    @Test
    void testAgregarCategoriaConImagen() throws Exception {

        String json = """
            {
                "nombre": "Hola111",
                "descripcion": "aaaaaaaaaaaaaa222"
            }
        """;

        // Parte JSON
        MockMultipartFile jsonPart = new MockMultipartFile(
                "categoria",  // clave debe coincidir con @RequestPart("categoria")
                "",
                "application/json",
                json.getBytes()
        );

        // Parte archivo
        MockMultipartFile filePart = new MockMultipartFile(
                "file",
                "imagen.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "contenido-de-imagen-ficticio".getBytes()
        );

        mockMvc.perform(multipart("/api/categoria/crearCategoriaConImagen")
                        .file(jsonPart)
                        .file(filePart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("Se hizo bien"));
    }

    @WithMockUser(username = "thiago2007crackz@gmail.com", roles = {"ADMIN"})
    @Test
    void testLeerCategoriaTodas() throws Exception {

        mockMvc.perform(get("/api/categoria/leerCategoriaTodas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));

    }

    @WithMockUser(username = "thiago2007crackz@gmail.com", roles = {"ADMIN"})
    @Test
    void testLeerCategoriaUna() throws Exception {

        mockMvc.perform(get("/api/categoria/leerCategoria/2"))
                .andExpect(status().isOk());
    }


    @WithMockUser(username = "thiago2007crackz@gmail.com", roles = {"ADMIN"})
    @Test
    void testModificarCategoriaUna() throws Exception {

        CategoriaUpdateRequest request = new CategoriaUpdateRequest();
        request.setNombre("Hola67");
        request.setDescripcion("La lenta sinfonia que nos da la vida a todos");

        mockMvc.perform(put("/api/categoria/modificarCategoria/2")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Se hizo bien")));
    }


    @WithMockUser(username = "thiago2007crackz@gmail.com", roles = {"ADMIN"})
    @Test
    void testBorrarCategoriaUna() throws Exception {

        mockMvc.perform(delete("/api/categoria/borrarCategoria/2"))
                .andExpect(status().isNoContent());
    }

    }

