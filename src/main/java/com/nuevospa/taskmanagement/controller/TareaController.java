package com.nuevospa.taskmanagement.controller;

import com.nuevospa.taskmanagement.api.TasksApi;
import com.nuevospa.taskmanagement.dto.TareaRequest;
import com.nuevospa.taskmanagement.dto.TareaResponse;
import com.nuevospa.taskmanagement.service.TareaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TareaController implements TasksApi {

    private final TareaService tareaService;

    public TareaController(TareaService tareaService) {
        this.tareaService = tareaService;
    }

    @Override
    public ResponseEntity<List<TareaResponse>> listarTareas(Long estadoId, Long usuarioAsignadoId) {
        return ResponseEntity.ok(tareaService.listarTareas(estadoId, usuarioAsignadoId));
    }

    @Override
    public ResponseEntity<TareaResponse> crearTarea(TareaRequest tareaRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tareaService.crear(tareaRequest));
    }

    @Override
    public ResponseEntity<TareaResponse> obtenerTarea(Long id) {
        return ResponseEntity.ok(tareaService.obtenerPorId(id));
    }

    @Override
    public ResponseEntity<TareaResponse> actualizarTarea(Long id, TareaRequest tareaRequest) {
        return ResponseEntity.ok(tareaService.actualizar(id, tareaRequest));
    }

    @Override
    public ResponseEntity<Void> eliminarTarea(Long id) {
        tareaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
