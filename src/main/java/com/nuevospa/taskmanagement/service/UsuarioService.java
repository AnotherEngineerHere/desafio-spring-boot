package com.nuevospa.taskmanagement.service;

import com.nuevospa.taskmanagement.dto.UsuarioResponse;
import com.nuevospa.taskmanagement.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.isActivo())
                .map(usuario -> {
                    UsuarioResponse dto = new UsuarioResponse();
                    dto.setId(usuario.getId());
                    dto.setNombre(usuario.getNombre());
                    dto.setEmail(usuario.getEmail());
                    dto.setRol(usuario.getRol());
                    return dto;
                })
                .toList();
    }
}
