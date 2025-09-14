package com.formaprogramada.ecommerce_backend.Domain.Service.Usuario;

import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario.Usuario;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Usuario.UsuarioRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UsuarioCacheProxy {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioCacheService cacheService;

    public UsuarioCacheProxy(UsuarioRepository usuarioRepository, UsuarioCacheService cacheService) {
        this.usuarioRepository = usuarioRepository;
        this.cacheService = cacheService;
    }

    @PostConstruct
    public void inicializarCache() {
        precargarUsuarios();
    }

    public void precargarUsuarios() {
        try {
            // Precargar lista completa
            List<Usuario> todosLosUsuarios = usuarioRepository.findAll();
            cacheService.actualizarListaCompleta(todosLosUsuarios);

            // Precargar usuarios individuales
            for (Usuario usuario : todosLosUsuarios) {
                cacheService.actualizarUsuarioEnCache(usuario);
            }

            System.out.println("Caché de usuarios inicializado con " + todosLosUsuarios.size() + " usuarios");
        } catch (Exception e) {
            System.err.println("Error al inicializar caché de usuarios: " + e.getMessage());
        }
    }

    // Método para precarga manual
    public void recargarCacheCompleto() {
        precargarUsuarios();
    }

    // Método para precarga de un usuario específico
    public void precargarUsuario(String gmail) {
        try {
            Optional<Usuario> usuario = usuarioRepository.buscarPorGmail(gmail);
            usuario.ifPresent(cacheService::actualizarUsuarioEnCache);
        } catch (Exception e) {
            System.err.println("Error al precargar usuario " + gmail + ": " + e.getMessage());
        }
    }
}
