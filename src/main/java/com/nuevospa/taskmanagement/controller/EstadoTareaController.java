package com.nuevospa.taskmanagement.controller;

import com.nuevospa.taskmanagement.api.TaskStatesApi;
import com.nuevospa.taskmanagement.dto.EstadoTareaResponse;
import com.nuevospa.taskmanagement.service.EstadoTareaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EstadoTareaController implements TaskStatesApi {

    private final EstadoTareaService estadoTareaService;

    public EstadoTareaController(EstadoTareaService estadoTareaService) {
        this.estadoTareaService = estadoTareaService;
    }

    @Override
    public ResponseEntity<List<EstadoTareaResponse>> listarEstados() {
        return ResponseEntity.ok(estadoTareaService.listarEstados());
    }
}
