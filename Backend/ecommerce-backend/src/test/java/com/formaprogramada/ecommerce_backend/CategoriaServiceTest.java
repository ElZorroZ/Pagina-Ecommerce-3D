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



    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAgregarCategoriaConImagen() throws Exception {
        CategoriaCrearRequest request = new CategoriaCrearRequest("nombre de prueba", "descripción de prueba");
        String categoriaJson = new ObjectMapper().writeValueAsString(request);

        MockMultipartFile categoriaPart = new MockMultipartFile(
                "categoria",
                "categoria.json",
                "application/json",
                categoriaJson.getBytes()
        );

        MockMultipartFile filePart = new MockMultipartFile(
                "file",
                "imagen.jpg",
                "image/jpeg",
                "contenido_falso".getBytes()
        );

        mockMvc.perform(multipart("/api/categoria/crearCategoriaConImagen")
                        .file(categoriaPart)
                        .file(filePart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
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
        request.setNombre("Hola99999");
        request.setDescripcion("La lenta sinfonia que nos da la vida a todos");

        mockMvc.perform(put("/api/categoria/modificarCategoria/16")
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

