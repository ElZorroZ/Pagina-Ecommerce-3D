package com.formaprogramada.ecommerce_backend.Domain.Service.Carrito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarritoCacheProxyService {

    @Autowired
    private CarritoService carritoService;

    public void precargarCarritoPorUsuarioId(int usuarioId) {
        carritoService.LeerUnCarrito(usuarioId);
    }
    public void precargarTodosLosCarritos(List<Integer> usuariosIds) {
        for (Integer id : usuariosIds) {
            precargarCarritoPorUsuarioId(id);
        }
    }
    public void precargarCarritoCompletoPorUsuarioId(int usuarioId) {
        carritoService.LeerUnCarritoCompleto(usuarioId);
    }

    // Método para precargar cache de varios usuarios
    public void precargarTodosLosCarritosCompletos(List<Integer> usuariosIds) {
        for (Integer id : usuariosIds) {
            precargarCarritoCompletoPorUsuarioId(id);  // <-- aquí debe llamar al método correcto
        }
    }
}

