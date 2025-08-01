package com.formaprogramada.ecommerce_backend.Domain.Service.Colaborador;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Colaborador.ColaboradorDTO;

import java.util.List;

public interface ColaboradorService {
    void alternarPermiso(String gmail);
    List<ColaboradorDTO> obtenerColaboradores();
}
