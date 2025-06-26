package com.formaprogramada.ecommerce_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario.Usuario;
import com.formaprogramada.ecommerce_backend.Domain.Repository.CategoriaRepository;
import com.formaprogramada.ecommerce_backend.Domain.Service.Impl.CategoriaServiceImpl;
import com.formaprogramada.ecommerce_backend.Domain.Service.Usuario.UsuarioService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioUpdate;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private UsuarioService usuarioService;

	@MockBean
	private CategoriaRepository categoriaRepository;

	@Autowired
	private CategoriaServiceImpl categoriaService;
	@WithMockUser(username = "thiago2007crackz@gmail.com", roles = {"CLIENTE"})
	@Test
	void testObtenerUsuario_Exitoso() throws Exception {
		Usuario usuario = new Usuario();
		usuario.setGmail("thiago2007crackz@gmail.com");
		usuario.setNombre("Thiago");

		Mockito.when(usuarioService.buscarPorGmail("thiago2007crackz@gmail.com"))
				.thenReturn(Optional.of(usuario));

		mockMvc.perform(get("/api/usuario/thiago2007crackz@gmail.com"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.gmail").value("thiago2007crackz@gmail.com"))
				.andExpect(jsonPath("$.nombre").value("Thiago"));
	}

	@Test
	void testObtenerUsuario_NoEncontrado() throws Exception {
		Mockito.when(usuarioService.buscarPorGmail("noexiste@gmail.com"))
				.thenReturn(Optional.empty());

		mockMvc.perform(get("/usuarios/noexiste@gmail.com"))
				.andExpect(status().isNotFound());
	}

	@WithMockUser(username = "thiago2007crackz@gmail.com", roles = {"CLIENTE"})
	@Test
	void testActualizarUsuario_Exitoso() throws Exception {
		UsuarioUpdate update = new UsuarioUpdate();
		update.setNombre("Nuevo Nombre");

		Usuario usuarioActualizado = new Usuario();
		usuarioActualizado.setGmail("thiago2007crackz@gmail.com");
		usuarioActualizado.setNombre("Nuevo Nombre");

		Mockito.when(usuarioService.actualizarUsuarioPorGmail(
						Mockito.eq("thiago2007crackz@gmail.com"),
						Mockito.any(UsuarioUpdate.class)))
				.thenReturn(usuarioActualizado);

		mockMvc.perform(put("/api/usuario/thiago2007crackz@gmail.com")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(update)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("Nuevo Nombre"));
	}

	@Test
	void testActualizarUsuario_BadRequest() throws Exception {
		UsuarioUpdate update = new UsuarioUpdate();
		update.setNombre("Nombre inválido");

		Mockito.when(usuarioService.actualizarUsuarioPorGmail(
						Mockito.eq("test@gmail.com"),
						Mockito.any(UsuarioUpdate.class)))
				.thenThrow(new IllegalArgumentException("Datos inválidos"));

		mockMvc.perform(put("/usuarios/test@gmail.com")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(update)))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("Datos inválidos"));
	}
}
