package com.nuevospa.taskmanagement;

import com.nuevospa.taskmanagement.dto.LoginRequest;
import com.nuevospa.taskmanagement.dto.LoginResponse;
import com.nuevospa.taskmanagement.dto.TareaRequest;
import com.nuevospa.taskmanagement.dto.TareaResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskManagementApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;
    private String adminToken;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        adminToken = obtenerToken("admin@nuevospa.cl", "admin123");
    }

    @Test
    @DisplayName("Application context loads successfully")
    void contextLoads() {
    }

    // =========================================================
    // Authentication
    // =========================================================
    @Nested
    @DisplayName("Authentication")
    class AuthTests {

        @Test
        @DisplayName("POST /api/auth/token — valid credentials return JWT token")
        void loginExitoso() {
            LoginRequest request = buildLoginRequest("admin@nuevospa.cl", "admin123");

            ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                    baseUrl + "/api/auth/token", request, LoginResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getToken()).isNotBlank();
            assertThat(response.getBody().getTipo()).isEqualTo("Bearer");
            assertThat(response.getBody().getExpiracion()).isPositive();
            assertThat(response.getBody().getUsuario()).isNotNull();
            assertThat(response.getBody().getUsuario().getEmail()).isEqualTo("admin@nuevospa.cl");
        }

        @Test
        @DisplayName("POST /api/auth/token — wrong password returns 401")
        void loginConPasswordIncorrecta() {
            LoginRequest request = buildLoginRequest("admin@nuevospa.cl", "wrongpassword");

            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/api/auth/token", request, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("POST /api/auth/token — non-existent email returns 401")
        void loginConEmailInexistente() {
            LoginRequest request = buildLoginRequest("noexiste@nuevospa.cl", "admin123");

            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/api/auth/token", request, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("POST /api/auth/token — regular user can authenticate")
        void loginUsuarioRegular() {
            LoginRequest request = buildLoginRequest("juan.perez@nuevospa.cl", "user1234");

            ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                    baseUrl + "/api/auth/token", request, LoginResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getUsuario().getRol()).isEqualTo("USER");
        }
    }

    // =========================================================
    // Security
    // =========================================================
    @Nested
    @DisplayName("Security")
    class SecurityTests {

        @Test
        @DisplayName("GET /api/tareas — no token returns 401/403")
        void accesoSinToken() {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    baseUrl + "/api/tareas", String.class);

            assertThat(response.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("GET /api/tareas — invalid token returns 401/403")
        void accesoConTokenInvalido() {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth("this.is.not.a.valid.token");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/api/tareas", HttpMethod.GET, entity, String.class);

            assertThat(response.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("GET /swagger-ui.html — accessible without token")
        void swaggerAccesibleSinAutenticacion() {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    baseUrl + "/swagger-ui.html", String.class);

            assertThat(response.getStatusCode()).isNotIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("GET /openapi.yml — accessible without token")
        void openapiYamlAccesibleSinAutenticacion() {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    baseUrl + "/openapi.yml", String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).contains("openapi:");
        }
    }

    // =========================================================
    // Tasks — CRUD
    // =========================================================
    @Nested
    @DisplayName("Tasks")
    class TareaTests {

        @Test
        @DisplayName("GET /api/tareas — returns all tasks")
        void listarTareas() {
            HttpEntity<Void> entity = new HttpEntity<>(authHeaders());

            ResponseEntity<List> response = restTemplate.exchange(
                    baseUrl + "/api/tareas", HttpMethod.GET, entity, List.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @DisplayName("GET /api/tareas?estadoId=1 — filters by status")
        void listarTareasFiltrandoPorEstado() {
            crearTareaRequest("Filtro estado", 1L, null);

            HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
            ResponseEntity<List> response = restTemplate.exchange(
                    baseUrl + "/api/tareas?estadoId=1", HttpMethod.GET, entity, List.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotEmpty();
        }

        @Test
        @DisplayName("GET /api/tareas?usuarioAsignadoId=2 — filters by assigned user")
        void listarTareasFiltrandoPorUsuario() {
            crearTareaRequest("Filtro usuario", 1L, 2L);

            HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
            ResponseEntity<List> response = restTemplate.exchange(
                    baseUrl + "/api/tareas?usuarioAsignadoId=2", HttpMethod.GET, entity, List.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotEmpty();
        }

        @Test
        @DisplayName("POST /api/tareas — creates task with required fields only")
        void crearTareaMinima() {
            TareaRequest request = new TareaRequest();
            request.setTitulo("Tarea mínima");
            request.setEstadoId(1L);

            HttpEntity<TareaRequest> entity = new HttpEntity<>(request, authHeaders());
            ResponseEntity<TareaResponse> response = restTemplate.exchange(
                    baseUrl + "/api/tareas", HttpMethod.POST, entity, TareaResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody().getId()).isNotNull();
            assertThat(response.getBody().getTitulo()).isEqualTo("Tarea mínima");
            assertThat(response.getBody().getUsuarioCreador()).isNotNull();
            assertThat(response.getBody().getFechaCreacion()).isNotNull();
        }

        @Test
        @DisplayName("POST /api/tareas — creates task with all fields")
        void crearTareaCompleta() {
            TareaRequest request = new TareaRequest();
            request.setTitulo("Tarea completa");
            request.setDescripcion("Descripción detallada");
            request.setEstadoId(1L);
            request.setUsuarioAsignadoId(2L);

            HttpEntity<TareaRequest> entity = new HttpEntity<>(request, authHeaders());
            ResponseEntity<TareaResponse> response = restTemplate.exchange(
                    baseUrl + "/api/tareas", HttpMethod.POST, entity, TareaResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody().getDescripcion()).isEqualTo("Descripción detallada");
            assertThat(response.getBody().getUsuarioAsignado()).isNotNull();
            assertThat(response.getBody().getUsuarioAsignado().getId()).isEqualTo(2L);
            assertThat(response.getBody().getEstado().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("POST /api/tareas — invalid estadoId returns 404")
        void crearTareaConEstadoInexistente() {
            TareaRequest request = new TareaRequest();
            request.setTitulo("Tarea inválida");
            request.setEstadoId(999L);

            HttpEntity<TareaRequest> entity = new HttpEntity<>(request, authHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/api/tareas", HttpMethod.POST, entity, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("GET /api/tareas/{id} — returns existing task")
        void obtenerTareaPorId() {
            Long id = crearTareaRequest("Obtener por ID", 1L, null);

            HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
            ResponseEntity<TareaResponse> response = restTemplate.exchange(
                    baseUrl + "/api/tareas/" + id, HttpMethod.GET, entity, TareaResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getId()).isEqualTo(id);
            assertThat(response.getBody().getTitulo()).isEqualTo("Obtener por ID");
        }

        @Test
        @DisplayName("GET /api/tareas/{id} — non-existent ID returns 404")
        void obtenerTareaNoExistente() {
            HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/api/tareas/99999", HttpMethod.GET, entity, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("PUT /api/tareas/{id} — updates title and status")
        void actualizarTarea() {
            Long id = crearTareaRequest("Original", 1L, null);

            TareaRequest update = new TareaRequest();
            update.setTitulo("Actualizada");
            update.setEstadoId(2L);

            HttpEntity<TareaRequest> entity = new HttpEntity<>(update, authHeaders());
            ResponseEntity<TareaResponse> response = restTemplate.exchange(
                    baseUrl + "/api/tareas/" + id, HttpMethod.PUT, entity, TareaResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getTitulo()).isEqualTo("Actualizada");
            assertThat(response.getBody().getEstado().getId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("PUT /api/tareas/{id} — non-existent ID returns 404")
        void actualizarTareaNoExistente() {
            TareaRequest update = new TareaRequest();
            update.setTitulo("No existe");
            update.setEstadoId(1L);

            HttpEntity<TareaRequest> entity = new HttpEntity<>(update, authHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/api/tareas/99999", HttpMethod.PUT, entity, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("DELETE /api/tareas/{id} — deletes existing task")
        void eliminarTarea() {
            Long id = crearTareaRequest("Para eliminar", 1L, null);
            HttpEntity<Void> entity = new HttpEntity<>(authHeaders());

            ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                    baseUrl + "/api/tareas/" + id, HttpMethod.DELETE, entity, Void.class);
            assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            ResponseEntity<String> getResponse = restTemplate.exchange(
                    baseUrl + "/api/tareas/" + id, HttpMethod.GET, entity, String.class);
            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("DELETE /api/tareas/{id} — non-existent ID returns 404")
        void eliminarTareaNoExistente() {
            HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/api/tareas/99999", HttpMethod.DELETE, entity, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    // =========================================================
    // Task Statuses
    // =========================================================
    @Nested
    @DisplayName("Task Statuses")
    class EstadoTests {

        @Test
        @DisplayName("GET /api/estados — returns 5 pre-loaded statuses")
        void listarEstados() {
            HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
            ResponseEntity<List> response = restTemplate.exchange(
                    baseUrl + "/api/estados", HttpMethod.GET, entity, List.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(5);
        }

        @Test
        @DisplayName("GET /api/estados — status names match expected catalog")
        void estadosContienenNombresEsperados() {
            HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
            ResponseEntity<List> response = restTemplate.exchange(
                    baseUrl + "/api/estados", HttpMethod.GET, entity, List.class);

            List<Map<String, Object>> estados = response.getBody();
            List<String> nombres = estados.stream()
                    .map(e -> (String) e.get("nombre"))
                    .toList();

            assertThat(nombres).containsExactlyInAnyOrder(
                    "PENDIENTE", "EN_PROGRESO", "EN_REVISION", "COMPLETADA", "CANCELADA");
        }
    }

    // =========================================================
    // Users
    // =========================================================
    @Nested
    @DisplayName("Users")
    class UsuarioTests {

        @Test
        @DisplayName("GET /api/usuarios — returns 3 pre-loaded users")
        void listarUsuarios() {
            HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
            ResponseEntity<List> response = restTemplate.exchange(
                    baseUrl + "/api/usuarios", HttpMethod.GET, entity, List.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(3);
        }

        @Test
        @DisplayName("GET /api/usuarios — response does not expose passwords")
        void usuariosNoExponenPassword() {
            HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
            ResponseEntity<List> response = restTemplate.exchange(
                    baseUrl + "/api/usuarios", HttpMethod.GET, entity, List.class);

            List<Map<String, Object>> usuarios = response.getBody();
            usuarios.forEach(u -> assertThat(u).doesNotContainKey("password"));
        }
    }

    // =========================================================
    // Helpers
    // =========================================================

    private Long crearTareaRequest(String titulo, Long estadoId, Long usuarioAsignadoId) {
        TareaRequest request = new TareaRequest();
        request.setTitulo(titulo);
        request.setEstadoId(estadoId);
        request.setUsuarioAsignadoId(usuarioAsignadoId);

        HttpEntity<TareaRequest> entity = new HttpEntity<>(request, authHeaders());
        ResponseEntity<TareaResponse> response = restTemplate.exchange(
                baseUrl + "/api/tareas", HttpMethod.POST, entity, TareaResponse.class);

        return response.getBody().getId();
    }

    private String obtenerToken(String email, String password) {
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                baseUrl + "/api/auth/token", buildLoginRequest(email, password), LoginResponse.class);
        return response.getBody().getToken();
    }

    private LoginRequest buildLoginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
