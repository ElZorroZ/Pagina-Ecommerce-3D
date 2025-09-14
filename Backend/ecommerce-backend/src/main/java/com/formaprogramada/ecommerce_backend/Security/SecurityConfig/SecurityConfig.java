package com.formaprogramada.ecommerce_backend.Security.SecurityConfig;
import com.formaprogramada.ecommerce_backend.Security.SecurityConfig.JWT.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.formaprogramada.ecommerce_backend.Security.SecurityConfig.JWT.JwtAuthenticationFilter;
import com.formaprogramada.ecommerce_backend.Security.SecurityConfig.JWT.JwtAuthenticationProvider;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
@Profile("!test")
@Configuration
@EnableMethodSecurity

public class SecurityConfig {
    private final JwtAuthenticationFilter jwtFilter;
    private final JwtAuthenticationProvider jwtProvider;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter, JwtAuthenticationProvider jwtProvider) {
        this.jwtFilter = jwtFilter;
        this.jwtProvider = jwtProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, DaoAuthenticationProvider daoAuthenticationProvider) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> corsConfigurationSource())
                .authorizeHttpRequests(auth -> auth
                        // ENDPOINTS PÚBLICOS
                        .requestMatchers("/api/auth/register", "/api/auth/validate", "/api/auth/login",
                                "/api/auth/refresh", "/api/usuario/confirmar-email",
                                "/api/auth/reset-password-request",
                                "/api/auth/reset-password/confirm",
                                "/api/auth/oauth2/success").permitAll()
                        .requestMatchers("/api/productos/busqueda/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categoria").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/productos", "/api/productos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/completo").permitAll()
                        .requestMatchers(
                                "/api/mp/auto-detect-environment",
                                "/api/mp/simulate-webhook",
                                "/api/mp/webhook",
                                "/api/mp/payment-info/**",
                                "/api/mp/check-payment/{pedidoId}",
                                "/api/mp/config-status",
                                "/api/mp/webhook-test",
                                "/api/mp/verificar-pago/**",
                                "/api/mp/webhook-debug"
                        ).permitAll()
                        // ENDPOINTS PROTEGIDOS
                        .requestMatchers(HttpMethod.POST, "/api/carrito/**").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/api/carrito/**").hasAnyRole("CLIENTE", "ADMIN", "COLABORADOR")
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("CLIENTE", "ADMIN", "COLABORADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/usuario/**").hasAnyRole("CLIENTE", "ADMIN", "COLABORADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/carrito/**").hasAnyRole("CLIENTE", "ADMIN", "COLABORADOR")
                        .requestMatchers(HttpMethod.POST, "/api/reviews").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.POST, "/api/pedido/**").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.DELETE, "/api/carrito/**").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/pedido/**").hasAnyRole("CLIENTE")
                        .requestMatchers(HttpMethod.POST, "/api/reviews/*/responder").hasAnyRole("COLABORADOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/productosAprobacion/AprobarProducto").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/productosAprobacion/crearAprobacionProducto").hasRole("COLABORADOR")
                        .requestMatchers(HttpMethod.POST, "/api/productosAprobacion/*/archivos").hasRole("COLABORADOR")
                        .requestMatchers(HttpMethod.GET, "/api/productosAprobacion/**").hasAnyRole("COLABORADOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/productosAprobacion/**").hasRole("COLABORADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/productosAprobacion/**").hasAnyRole("COLABORADOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                // Autenticación
                .authenticationProvider(daoAuthenticationProvider)
                .authenticationProvider(jwtProvider)
                // JWT filter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                // OAuth2 login
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/api/auth/oauth2/success", true) // endpoint donde se maneja token JWT
                )
                // Deshabilitar login clásico
                .formLogin(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Agregá tu dominio de Netlify
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5500",
                "http://127.0.0.1:5500",
                "http://localhost:5501",
                "http://127.0.0.1:5501",
                "https://forma-programada.netlify.app"
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }



    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(CustomUserDetailsService userDetailsService,
                                                               PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}