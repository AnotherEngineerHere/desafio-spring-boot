package com.nuevospa.taskmanagement.service;

import com.nuevospa.taskmanagement.dto.EstadoTareaResponse;
import com.nuevospa.taskmanagement.repository.EstadoTareaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class EstadoTareaService {

    private final EstadoTareaRepository estadoTareaRepository;

    public EstadoTareaService(EstadoTareaRepository estadoTareaRepository) {
        this.estadoTareaRepository = estadoTareaRepository;
    }

    public List<EstadoTareaResponse> listarEstados() {
        return estadoTareaRepository.findAll().stream()
                .map(estado -> {
                    EstadoTareaResponse dto = new EstadoTareaResponse();
                    dto.setId(estado.getId());
                    dto.setNombre(estado.getNombre());
                    dto.setDescripcion(estado.getDescripcion());
                    return dto;
                })
                .toList();
    }
}
