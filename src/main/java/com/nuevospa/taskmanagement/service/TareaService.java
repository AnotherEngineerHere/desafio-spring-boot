package com.nuevospa.taskmanagement.service;

import com.nuevospa.taskmanagement.dto.EstadoTareaResponse;
import com.nuevospa.taskmanagement.dto.TareaRequest;
import com.nuevospa.taskmanagement.dto.TareaResponse;
import com.nuevospa.taskmanagement.dto.UsuarioResponse;
import com.nuevospa.taskmanagement.exception.ResourceNotFoundException;
import com.nuevospa.taskmanagement.model.EstadoTarea;
import com.nuevospa.taskmanagement.model.Tarea;
import com.nuevospa.taskmanagement.model.Usuario;
import com.nuevospa.taskmanagement.repository.EstadoTareaRepository;
import com.nuevospa.taskmanagement.repository.TareaRepository;
import com.nuevospa.taskmanagement.repository.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@Transactional
public class TareaService {

    private final TareaRepository tareaRepository;
    private final EstadoTareaRepository estadoTareaRepository;
    private final UsuarioRepository usuarioRepository;

    public TareaService(TareaRepository tareaRepository,
                        EstadoTareaRepository estadoTareaRepository,
                        UsuarioRepository usuarioRepository) {
        this.tareaRepository = tareaRepository;
        this.estadoTareaRepository = estadoTareaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<TareaResponse> listarTareas(Long estadoId, Long usuarioAsignadoId) {
        List<Tarea> tareas;

        if (estadoId != null && usuarioAsignadoId != null) {
            tareas = tareaRepository.findByEstadoIdAndUsuarioAsignadoId(estadoId, usuarioAsignadoId);
        } else if (estadoId != null) {
            tareas = tareaRepository.findByEstadoId(estadoId);
        } else if (usuarioAsignadoId != null) {
            tareas = tareaRepository.findByUsuarioAsignadoId(usuarioAsignadoId);
        } else {
            tareas = tareaRepository.findAll();
        }

        return tareas.stream().map(this::mapearTarea).toList();
    }

    @Transactional(readOnly = true)
    public TareaResponse obtenerPorId(Long id) {
        return tareaRepository.findById(id)
                .map(this::mapearTarea)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea", id));
    }

    public TareaResponse crear(TareaRequest request) {
        EstadoTarea estado = obtenerEstado(request.getEstadoId());
        Usuario creador = obtenerUsuarioAutenticado();
        Usuario asignado = request.getUsuarioAsignadoId() != null
                ? obtenerUsuario(request.getUsuarioAsignadoId()) : null;

        Tarea tarea = new Tarea();
        tarea.setTitulo(request.getTitulo());
        tarea.setDescripcion(request.getDescripcion());
        tarea.setEstado(estado);
        tarea.setUsuarioCreador(creador);
        tarea.setUsuarioAsignado(asignado);

        if (request.getFechaVencimiento() != null) {
            tarea.setFechaVencimiento(request.getFechaVencimiento().toLocalDateTime());
        }

        return mapearTarea(tareaRepository.save(tarea));
    }

    public TareaResponse actualizar(Long id, TareaRequest request) {
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea", id));

        EstadoTarea estado = obtenerEstado(request.getEstadoId());
        Usuario asignado = request.getUsuarioAsignadoId() != null
                ? obtenerUsuario(request.getUsuarioAsignadoId()) : null;

        tarea.setTitulo(request.getTitulo());
        tarea.setDescripcion(request.getDescripcion());
        tarea.setEstado(estado);
        tarea.setUsuarioAsignado(asignado);

        if (request.getFechaVencimiento() != null) {
            tarea.setFechaVencimiento(request.getFechaVencimiento().toLocalDateTime());
        } else {
            tarea.setFechaVencimiento(null);
        }

        return mapearTarea(tareaRepository.save(tarea));
    }

    public void eliminar(Long id) {
        if (!tareaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tarea", id);
        }
        tareaRepository.deleteById(id);
    }

    private EstadoTarea obtenerEstado(Long estadoId) {
        return estadoTareaRepository.findById(estadoId)
                .orElseThrow(() -> new ResourceNotFoundException("EstadoTarea", estadoId));
    }

    private Usuario obtenerUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", usuarioId));
    }

    private Usuario obtenerUsuarioAutenticado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
    }

    private TareaResponse mapearTarea(Tarea tarea) {
        TareaResponse dto = new TareaResponse();
        dto.setId(tarea.getId());
        dto.setTitulo(tarea.getTitulo());
        dto.setDescripcion(tarea.getDescripcion());
        dto.setFechaCreacion(toOffsetDateTime(tarea.getFechaCreacion()));
        dto.setFechaActualizacion(toOffsetDateTime(tarea.getFechaActualizacion()));
        dto.setFechaVencimiento(toOffsetDateTime(tarea.getFechaVencimiento()));
        dto.setEstado(mapearEstado(tarea.getEstado()));
        dto.setUsuarioCreador(mapearUsuario(tarea.getUsuarioCreador()));
        dto.setUsuarioAsignado(tarea.getUsuarioAsignado() != null
                ? mapearUsuario(tarea.getUsuarioAsignado()) : null);
        return dto;
    }

    private EstadoTareaResponse mapearEstado(EstadoTarea estado) {
        EstadoTareaResponse dto = new EstadoTareaResponse();
        dto.setId(estado.getId());
        dto.setNombre(estado.getNombre());
        dto.setDescripcion(estado.getDescripcion());
        return dto;
    }

    private UsuarioResponse mapearUsuario(Usuario usuario) {
        UsuarioResponse dto = new UsuarioResponse();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        dto.setRol(usuario.getRol());
        return dto;
    }

    private OffsetDateTime toOffsetDateTime(LocalDateTime ldt) {
        return ldt != null ? ldt.atOffset(ZoneOffset.UTC) : null;
    }
}
