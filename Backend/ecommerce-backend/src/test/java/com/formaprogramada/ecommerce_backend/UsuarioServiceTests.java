package com.formaprogramada.ecommerce_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.UsuarioRegistroRequest;
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

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioServiceTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private JwtService jwtService;
	@MockBean
	private AuthenticationManager authManager;

	@Test
	void registerUsuario_retorna200YMensaje() throws Exception {
		UsuarioRegistroRequest request = UsuarioRegistroRequest.builder()
				.nombre("Thiago")
				.apellido("Velazquez")
				.gmail("testregister@example.com")
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
		Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);

		// Cuando authManager.authenticate se llame, devuelve la Authentication mockeada
		Mockito.when(authManager.authenticate(Mockito.any())).thenReturn(authentication);

		// Simular que jwtService genera tokens
		Mockito.when(jwtService.generateAccessToken(Mockito.anyMap(), Mockito.eq(gmail))).thenReturn("token123");
		Mockito.when(jwtService.generateRefreshToken(Mockito.anyMap(), Mockito.eq(gmail))).thenReturn("refreshToken123");

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
		Mockito.when(authManager.authenticate(Mockito.any()))
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