package com.nuevospa.taskmanagement.config;

import com.nuevospa.taskmanagement.model.EstadoTarea;
import com.nuevospa.taskmanagement.model.Usuario;
import com.nuevospa.taskmanagement.repository.EstadoTareaRepository;
import com.nuevospa.taskmanagement.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final EstadoTareaRepository estadoTareaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(EstadoTareaRepository estadoTareaRepository,
                           UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder) {
        this.estadoTareaRepository = estadoTareaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        cargarEstadosTarea();
        cargarUsuarios();
    }

    private void cargarEstadosTarea() {
        if (estadoTareaRepository.count() == 0) {
            List<EstadoTarea> estados = List.of(
                    new EstadoTarea("PENDIENTE", "Tarea pendiente de inicio"),
                    new EstadoTarea("EN_PROGRESO", "Tarea en curso de desarrollo"),
                    new EstadoTarea("EN_REVISION", "Tarea en proceso de revisión"),
                    new EstadoTarea("COMPLETADA", "Tarea finalizada exitosamente"),
                    new EstadoTarea("CANCELADA", "Tarea cancelada y sin efecto")
            );
            estadoTareaRepository.saveAll(estados);
            log.info("Estados de tarea cargados: {} registros", estados.size());
        }
    }

    private void cargarUsuarios() {
        if (usuarioRepository.count() == 0) {
            List<Usuario> usuarios = List.of(
                    new Usuario(
                            "Administrador Sistema",
                            "admin@nuevospa.cl",
                            passwordEncoder.encode("admin123"),
                            "ADMIN"
                    ),
                    new Usuario(
                            "Juan Pérez",
                            "juan.perez@nuevospa.cl",
                            passwordEncoder.encode("user1234"),
                            "USER"
                    ),
                    new Usuario(
                            "María González",
                            "maria.gonzalez@nuevospa.cl",
                            passwordEncoder.encode("user1234"),
                            "USER"
                    )
            );
            usuarioRepository.saveAll(usuarios);
            log.info("Usuarios cargados: {} registros", usuarios.size());
        }
    }
}
