package com.formaprogramada.ecommerce_backend.Domain.Service.Colaborador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ColaboradorCacheProxyService {

    private final ColaboradorService colaboradorService;

    @Autowired
    public ColaboradorCacheProxyService(ColaboradorService colaboradorService) {
        this.colaboradorService = colaboradorService;
    }

    // Cargar todos los colaboradores al caché
    public void precargarColaboradores() {
        colaboradorService.obtenerColaboradores(); // ya cachea en el método
    }
}

