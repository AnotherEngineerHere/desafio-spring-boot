package com.nuevospa.taskmanagement.controller;

import com.nuevospa.taskmanagement.api.UsersApi;
import com.nuevospa.taskmanagement.dto.UsuarioResponse;
import com.nuevospa.taskmanagement.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UsuarioController implements UsersApi {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }
}
