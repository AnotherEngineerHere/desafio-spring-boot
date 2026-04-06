package com.nuevospa.taskmanagement;

import com.nuevospa.taskmanagement.dto.LoginRequest;
import com.nuevospa.taskmanagement.dto.LoginResponse;
import com.nuevospa.taskmanagement.dto.TareaRequest;
import com.nuevospa.taskmanagement.dto.TareaResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskManagementApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        jwtToken = obtenerToken("admin@nuevospa.cl", "admin123");
    }

    @Test
    @DisplayName("El contexto de la aplicación carga correctamente")
    void contextLoads() {
    }

    @Test
    @DisplayName("Login exitoso retorna token JWT")
    void loginExitoso() {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@nuevospa.cl");
        request.setPassword("admin123");

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                baseUrl + "/api/auth/token", request, LoginResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotBlank();
        assertThat(response.getBody().getTipo()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("Login fallido retorna 401")
    void loginFallido() {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@nuevospa.cl");
        request.setPassword("wrongpassword");

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/auth/token", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Acceso sin token retorna 403")
    void accesoSinToken() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/tareas", String.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Listar tareas con token válido")
    void listarTareas() {
        HttpHeaders headers = crearHeadersConToken();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                baseUrl + "/api/tareas", HttpMethod.GET, entity, List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("Crear tarea con datos válidos")
    void crearTarea() {
        TareaRequest request = new TareaRequest();
        request.setTitulo("Tarea de prueba");
        request.setDescripcion("Descripción de prueba");
        request.setEstadoId(1L);

        HttpHeaders headers = crearHeadersConToken();
        HttpEntity<TareaRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<TareaResponse> response = restTemplate.exchange(
                baseUrl + "/api/tareas", HttpMethod.POST, entity, TareaResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitulo()).isEqualTo("Tarea de prueba");
        assertThat(response.getBody().getId()).isNotNull();
    }

    @Test
    @DisplayName("Listar estados de tarea")
    void listarEstados() {
        HttpHeaders headers = crearHeadersConToken();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                baseUrl + "/api/estados", HttpMethod.GET, entity, List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    @DisplayName("CRUD completo de tarea")
    void crudCompletoTarea() {
        HttpHeaders headers = crearHeadersConToken();

        // Crear
        TareaRequest crearRequest = new TareaRequest();
        crearRequest.setTitulo("Tarea CRUD");
        crearRequest.setEstadoId(1L);
        HttpEntity<TareaRequest> crearEntity = new HttpEntity<>(crearRequest, headers);
        ResponseEntity<TareaResponse> crearResponse = restTemplate.exchange(
                baseUrl + "/api/tareas", HttpMethod.POST, crearEntity, TareaResponse.class);
        assertThat(crearResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long id = crearResponse.getBody().getId();

        // Leer
        HttpEntity<Void> getEntity = new HttpEntity<>(headers);
        ResponseEntity<TareaResponse> getResponse = restTemplate.exchange(
                baseUrl + "/api/tareas/" + id, HttpMethod.GET, getEntity, TareaResponse.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Actualizar
        TareaRequest actualizarRequest = new TareaRequest();
        actualizarRequest.setTitulo("Tarea CRUD actualizada");
        actualizarRequest.setEstadoId(2L);
        HttpEntity<TareaRequest> actualizarEntity = new HttpEntity<>(actualizarRequest, headers);
        ResponseEntity<TareaResponse> actualizarResponse = restTemplate.exchange(
                baseUrl + "/api/tareas/" + id, HttpMethod.PUT, actualizarEntity, TareaResponse.class);
        assertThat(actualizarResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualizarResponse.getBody().getTitulo()).isEqualTo("Tarea CRUD actualizada");

        // Eliminar
        ResponseEntity<Void> eliminarResponse = restTemplate.exchange(
                baseUrl + "/api/tareas/" + id, HttpMethod.DELETE, getEntity, Void.class);
        assertThat(eliminarResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verificar eliminación
        ResponseEntity<String> notFoundResponse = restTemplate.exchange(
                baseUrl + "/api/tareas/" + id, HttpMethod.GET, getEntity, String.class);
        assertThat(notFoundResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private String obtenerToken(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                baseUrl + "/api/auth/token", request, LoginResponse.class);

        return response.getBody().getToken();
    }

    private HttpHeaders crearHeadersConToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
