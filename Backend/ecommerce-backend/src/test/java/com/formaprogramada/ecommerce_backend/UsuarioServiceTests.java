package com.formaprogramada.ecommerce_backend;

import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario;
import com.formaprogramada.ecommerce_backend.Domain.Service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

import org.springframework.test.annotation.Commit;

@SpringBootTest
class UsuarioServiceTests {

	@Autowired
	private UsuarioService usuarioService;

	@Test
	void listarUsuarios_muestraTodos_y_assertNoVacio() {
		List<Usuario> usuarios = usuarioService.listarTodos();

		usuarios.forEach(u -> System.out.println("Usuario: " + u.getGmail()));

		assertFalse(usuarios.isEmpty(), "La lista de usuarios no debería estar vacía");
	}

	@Test
	void registrarUsuario_conGmailDuplicado_lanzaExcepcion() {
		Usuario usuario1 = Usuario.builder()
				.nombre("User1")
				.apellido("Test")
				.gmail("duplicado@example.com")
				.password("password1")
				.build();

		Usuario usuario2 = Usuario.builder()
				.nombre("User2")
				.apellido("Test")
				.gmail("duplicado@example.com")
				.password("password2")
				.build();

		usuarioService.registrarUsuario(usuario1);

		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
			usuarioService.registrarUsuario(usuario2);
		});

		assertEquals("Ya existe un usuario con ese Gmail.", thrown.getMessage());
	}
}

