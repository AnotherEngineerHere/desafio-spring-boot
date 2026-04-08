package com.nuevospa.taskmanagement.service;

import com.nuevospa.taskmanagement.dto.TareaRequest;
import com.nuevospa.taskmanagement.dto.TareaResponse;
import com.nuevospa.taskmanagement.exception.ResourceNotFoundException;
import com.nuevospa.taskmanagement.model.EstadoTarea;
import com.nuevospa.taskmanagement.model.Tarea;
import com.nuevospa.taskmanagement.model.Usuario;
import com.nuevospa.taskmanagement.repository.EstadoTareaRepository;
import com.nuevospa.taskmanagement.repository.TareaRepository;
import com.nuevospa.taskmanagement.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TareaService Unit Tests")
class TareaServiceTest {

    @Mock
    private TareaRepository tareaRepository;

    @Mock
    private EstadoTareaRepository estadoTareaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private TareaService tareaService;

    private Usuario usuarioAdmin;
    private EstadoTarea estadoPendiente;
    private Tarea tareaEjemplo;

    @BeforeEach
    void setUp() {
        usuarioAdmin = new Usuario("Admin", "admin@nuevospa.cl", "encoded", "ADMIN");
        usuarioAdmin.setId(1L);

        estadoPendiente = new EstadoTarea("PENDIENTE", "Tarea pendiente de inicio");
        estadoPendiente.setId(1L);

        tareaEjemplo = new Tarea();
        tareaEjemplo.setId(1L);
        tareaEjemplo.setTitulo("Tarea de prueba");
        tareaEjemplo.setDescripcion("Descripción");
        tareaEjemplo.setEstado(estadoPendiente);
        tareaEjemplo.setUsuarioCreador(usuarioAdmin);
        tareaEjemplo.setFechaCreacion(LocalDateTime.now());
        tareaEjemplo.setFechaActualizacion(LocalDateTime.now());

        mockSecurityContext("admin@nuevospa.cl");
    }

    @Nested
    @DisplayName("listarTareas")
    class ListarTareasTests {

        @Test
        @DisplayName("No filters — returns all tasks")
        void sinFiltros_retornaTodas() {
            when(tareaRepository.findAll()).thenReturn(List.of(tareaEjemplo));

            List<TareaResponse> result = tareaService.listarTareas(null, null);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitulo()).isEqualTo("Tarea de prueba");
            verify(tareaRepository).findAll();
        }

        @Test
        @DisplayName("estadoId filter — delegates to findByEstadoId")
        void conEstadoId_delegaAlRepositorio() {
            when(tareaRepository.findByEstadoId(1L)).thenReturn(List.of(tareaEjemplo));

            List<TareaResponse> result = tareaService.listarTareas(1L, null);

            assertThat(result).hasSize(1);
            verify(tareaRepository).findByEstadoId(1L);
            verify(tareaRepository, never()).findAll();
        }

        @Test
        @DisplayName("usuarioAsignadoId filter — delegates to findByUsuarioAsignadoId")
        void conUsuarioId_delegaAlRepositorio() {
            when(tareaRepository.findByUsuarioAsignadoId(2L)).thenReturn(List.of());

            List<TareaResponse> result = tareaService.listarTareas(null, 2L);

            assertThat(result).isEmpty();
            verify(tareaRepository).findByUsuarioAsignadoId(2L);
        }

        @Test
        @DisplayName("Both filters — delegates to findByEstadoIdAndUsuarioAsignadoId")
        void conAmbosFiltros_delegaAlRepositorio() {
            when(tareaRepository.findByEstadoIdAndUsuarioAsignadoId(1L, 2L))
                    .thenReturn(List.of(tareaEjemplo));

            List<TareaResponse> result = tareaService.listarTareas(1L, 2L);

            assertThat(result).hasSize(1);
            verify(tareaRepository).findByEstadoIdAndUsuarioAsignadoId(1L, 2L);
        }
    }

    @Nested
    @DisplayName("obtenerPorId")
    class ObtenerPorIdTests {

        @Test
        @DisplayName("Existing ID — returns mapped DTO")
        void idExistente_retornaDTO() {
            when(tareaRepository.findById(1L)).thenReturn(Optional.of(tareaEjemplo));

            TareaResponse result = tareaService.obtenerPorId(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitulo()).isEqualTo("Tarea de prueba");
            assertThat(result.getEstado()).isNotNull();
            assertThat(result.getUsuarioCreador()).isNotNull();
        }

        @Test
        @DisplayName("Non-existent ID — throws ResourceNotFoundException")
        void idNoExistente_lanzaExcepcion() {
            when(tareaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tareaService.obtenerPorId(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("crear")
    class CrearTests {

        @Test
        @DisplayName("Valid request — saves and returns DTO")
        void datosValidos_guardaYRetornaDTO() {
            TareaRequest request = new TareaRequest();
            request.setTitulo("Nueva tarea");
            request.setEstadoId(1L);

            when(estadoTareaRepository.findById(1L)).thenReturn(Optional.of(estadoPendiente));
            when(usuarioRepository.findByEmail("admin@nuevospa.cl")).thenReturn(Optional.of(usuarioAdmin));
            when(tareaRepository.save(any(Tarea.class))).thenAnswer(inv -> {
                Tarea t = inv.getArgument(0);
                t.setId(10L);
                t.setFechaCreacion(LocalDateTime.now());
                t.setFechaActualizacion(LocalDateTime.now());
                return t;
            });

            TareaResponse result = tareaService.crear(request);

            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getTitulo()).isEqualTo("Nueva tarea");
            verify(tareaRepository).save(any(Tarea.class));
        }

        @Test
        @DisplayName("Invalid estadoId — throws ResourceNotFoundException")
        void estadoInvalido_lanzaExcepcion() {
            TareaRequest request = new TareaRequest();
            request.setTitulo("Tarea");
            request.setEstadoId(999L);

            when(estadoTareaRepository.findById(999L)).thenReturn(Optional.empty());
            when(usuarioRepository.findByEmail("admin@nuevospa.cl")).thenReturn(Optional.of(usuarioAdmin));

            assertThatThrownBy(() -> tareaService.crear(request))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(tareaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("eliminar")
    class EliminarTests {

        @Test
        @DisplayName("Existing ID — deletes successfully")
        void idExistente_eliminaCorrectamente() {
            when(tareaRepository.existsById(1L)).thenReturn(true);

            tareaService.eliminar(1L);

            verify(tareaRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Non-existent ID — throws ResourceNotFoundException")
        void idNoExistente_lanzaExcepcion() {
            when(tareaRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> tareaService.eliminar(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(tareaRepository, never()).deleteById(any());
        }
    }

    // =========================================================
    // Helpers
    // =========================================================

    private void mockSecurityContext(String email) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(authentication.getName()).thenReturn(email);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
