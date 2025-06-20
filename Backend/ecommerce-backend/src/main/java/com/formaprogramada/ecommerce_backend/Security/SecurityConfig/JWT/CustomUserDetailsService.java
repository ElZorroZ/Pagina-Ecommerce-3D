package com.formaprogramada.ecommerce_backend.Security.SecurityConfig.JWT;

import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario;
import com.formaprogramada.ecommerce_backend.Domain.Repository.UsuarioRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String gmail) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.buscarPorGmail(gmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        String rol = Boolean.TRUE.equals(usuario.getPermiso()) ? "CLIENTE" : "ADMIN";


        return User.builder()
                .username(usuario.getGmail())
                .password(usuario.getPassword())
                .roles(rol) // Usa el rol mapeado a partir del permiso
                .build();
    }
}

