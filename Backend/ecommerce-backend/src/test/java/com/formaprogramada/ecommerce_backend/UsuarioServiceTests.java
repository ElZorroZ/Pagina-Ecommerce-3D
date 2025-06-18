package com.formaprogramada.ecommerce_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formaprogramada.ecommerce_backend.Domain.Service.TokenVerificacionService;
import com.formaprogramada.ecommerce_backend.Domain.Service.UsuarioService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.UsuarioRegistroRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.TokenVerificacion;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.UsuarioEntity;
import com.formaprogramada.ecommerce_backend.Security.SecurityConfig.JWT.CustomUserDetailsService;
import com.formaprogramada.ecommerce_backend.Security.SecurityConfig.JWT.JwtService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
@SpringBootTest
@AutoConfigureMockMvc
class UsuarioServiceTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private TokenVerificacionService tokenVerificacionService;

	@MockBean
	private UsuarioService usuarioService;

	@MockBean
	private JwtService jwtService;
	@MockBean
	private AuthenticationManager authManager;

	@Test
	public void testValidateEndpointBasic() throws Exception {
		mockMvc.perform(get("/validate").param("token", "alguntoken").with(user("user").roles("USER")))
				.andExpect(status().isNotFound()); // si da 404 igual, el controlador no está cargado
	}
	@Test
	public void validarEmail_conTokenValido_retorna200() throws Exception {
		var usuarioEntity = new UsuarioEntity();
		usuarioEntity.setId(1);

		var tokenVerificacion = new TokenVerificacion();
		tokenVerificacion.setUsuario(usuarioEntity);

		when(tokenVerificacionService.validarToken("tokenValido")).thenReturn(Optional.of(tokenVerificacion));
		doAnswer(invocation -> null).when(usuarioService).actualizarUsuario(any());

		mockMvc.perform(get("/api/auth/validate")
						.param("token", "tokenValido")
						.with(user("testregisteerrr@example.com").password("password123").roles("USER")))
				.andExpect(status().isOk())
				.andExpect(content().string("Usuario validado correctamente."));
	}


	@Test
	void registerUsuario_retorna200YMensaje() throws Exception {
		UsuarioRegistroRequest request = UsuarioRegistroRequest.builder()
				.nombre("Thiago")
				.apellido("Velazquez")
				.gmail("testregisteerrr@example.com")
				.password("password123")
				.build();

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Usuario registrado correctamente")));
	}

	@Test
	void login_conCredencialesValidas_devuelveTokens() throws Exception {
		String gmail = "test@login.com";
		String password = "password123";

		// Simular UserDetails con roles
		UserDetails userDetails = User.withUsername(gmail)
				.password(password)
				.roles("CLIENTE")
				.build();


		// Simular Authentication que devuelve el authManager
		Authentication authentication = Mockito.mock(Authentication.class);
		when(authentication.getPrincipal()).thenReturn(userDetails);

		// Cuando authManager.authenticate se llame, devuelve la Authentication mockeada
		when(authManager.authenticate(any())).thenReturn(authentication);

		// Simular que jwtService genera tokens
		when(jwtService.generateAccessToken(Mockito.anyMap(), Mockito.eq(gmail))).thenReturn("token123");
		when(jwtService.generateRefreshToken(Mockito.anyMap(), Mockito.eq(gmail))).thenReturn("refreshToken123");

		// Crear JSON para el body de la request
		String jsonBody = """
            {
                "gmail": "%s",
                "password": "%s"
            }
            """.formatted(gmail, password);

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(jsonBody))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("token123"))
				.andExpect(jsonPath("$.refreshToken").value("refreshToken123"));
	}

	@Test
	void login_conCredencialesInvalidas_devuelveUnauthorized() throws Exception {
		String gmail = "test@login.com";
		String password = "wrongpassword";

		// Cuando authManager.authenticate lance excepción
		when(authManager.authenticate(any()))
				.thenThrow(new BadCredentialsException("Credenciales inválidas"));

		String jsonBody = """
            {
                "gmail": "%s",
                "password": "%s"
            }
            """.formatted(gmail, password);

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(jsonBody))
				.andExpect(status().isUnauthorized())
				.andExpect(content().string("Credenciales inválidas"));
	}
}