package com.formaprogramada.ecommerce_backend.Security.SecurityConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Permitir login y register sin autenticación
                        .requestMatchers("/api/auth/register", "/api/auth/validate", "/api/auth/login", "/api/auth/refresh").permitAll()

                        // Permitir solo GET a Cliente
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("CLIENTE", "ADMIN")

                        // Permitir solo Admin para POST, PUT, DELETE
                        .requestMatchers(HttpMethod.POST, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .authenticationProvider(jwtProvider)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
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