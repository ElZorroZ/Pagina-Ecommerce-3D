package com.formaprogramada.ecommerce_backend.Security.SecurityConfig.JWT;

import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario.Usuario;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Usuario.UsuarioRepository;
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

        String rol = "";
        if (usuario.getPermiso()==0){
            rol = "CLIENTE";
        } else if (usuario.getPermiso()==1) {
            rol = "ADMIN";
        } else if (usuario.getPermiso()==2) {
            rol = "COLABORADOR";

        }

        return User.builder()
                .username(usuario.getGmail())
                .password(usuario.getPassword())
                .roles(rol) // Usa el rol mapeado a partir del permiso
                .build();
    }
}

