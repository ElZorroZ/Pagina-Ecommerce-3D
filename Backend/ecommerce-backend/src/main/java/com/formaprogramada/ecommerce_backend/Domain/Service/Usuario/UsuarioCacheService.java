package com.formaprogramada.ecommerce_backend.Domain.Service.Usuario;

import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario.Usuario;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Usuario.UsuarioRepository;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;
import java.util.List;

import java.util.Optional;

@Service
public class UsuarioCacheService {

    @Cacheable(value = "usuarios", key = "#gmail")
    public Optional<Usuario> buscarPorGmailCached(String gmail, UsuarioRepository usuarioRepository) {
        return usuarioRepository.buscarPorGmail(gmail);
    }

    @Cacheable(value = "usuariosPorId", key = "#id")
    public Optional<Usuario> buscarPorIdCached(int id, UsuarioRepository usuarioRepository) {
        return usuarioRepository.buscarPorId(id);
    }

    @Cacheable(value = "todosLosUsuarios")
    public List<Usuario> listarTodosCached(UsuarioRepository usuarioRepository) {
        return usuarioRepository.findAll();
    }

    @Cacheable(value = "existeUsuario", key = "#gmail")
    public boolean existePorGmailCached(String gmail, UsuarioRepository usuarioRepository) {
        return usuarioRepository.existePorGmail(gmail);
    }

    // Métodos para actualizar caché específico
    @Caching(
            put = {
                    @CachePut(value = "usuarios", key = "#usuario.gmail"),
                    @CachePut(value = "usuariosPorId", key = "#usuario.id")
            }
    )
    public Usuario actualizarUsuarioEnCache(Usuario usuario) {
        return usuario;
    }


    // Evict específico por Gmail
    @CacheEvict(value = {"usuarios", "existeUsuario"}, key = "#gmail")
    public void evictUsuarioPorGmail(String gmail) {
        // Método para invalidar caché específico por gmail
    }

    // Evict específico por ID
    @CacheEvict(value = "usuariosPorId", key = "#id")
    public void evictUsuarioPorId(int id) {
        // Método para invalidar caché específico por id
    }

    // Actualizar lista completa sin invalidar cachés individuales
    @CachePut(value = "todosLosUsuarios")
    public List<Usuario> actualizarListaCompleta(List<Usuario> usuarios) {
        return usuarios;
    }
}
