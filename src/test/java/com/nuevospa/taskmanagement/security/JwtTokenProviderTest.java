package com.nuevospa.taskmanagement.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtTokenProvider Unit Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    // Base64-encoded 256-bit key (valid for HS256)
    private static final String TEST_SECRET =
            "dGVzdFNlY3JldEtleUZvclRlc3RpbmdQdXJwb3NlczEyMzQ1Njc4OTA=";
    private static final long TEST_EXPIRATION = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", TEST_EXPIRATION);
    }

    @Test
    @DisplayName("generarToken — returns non-blank JWT string")
    void generarToken_retornaTokenNoVacio() {
        UserDetails user = buildUserDetails("user@test.cl");

        String token = jwtTokenProvider.generarToken(user);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
    }

    @Test
    @DisplayName("obtenerEmailDelToken — extracts correct email from token")
    void obtenerEmail_retornaEmailCorrecto() {
        UserDetails user = buildUserDetails("user@test.cl");
        String token = jwtTokenProvider.generarToken(user);

        String email = jwtTokenProvider.obtenerEmailDelToken(token);

        assertThat(email).isEqualTo("user@test.cl");
    }

    @Test
    @DisplayName("validarToken — valid token returns true")
    void validarToken_tokenValido_retornaTrue() {
        UserDetails user = buildUserDetails("user@test.cl");
        String token = jwtTokenProvider.generarToken(user);

        boolean valid = jwtTokenProvider.validarToken(token, user);

        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("validarToken — token for different user returns false")
    void validarToken_otroUsuario_retornaFalse() {
        UserDetails userA = buildUserDetails("a@test.cl");
        UserDetails userB = buildUserDetails("b@test.cl");
        String tokenForA = jwtTokenProvider.generarToken(userA);

        boolean valid = jwtTokenProvider.validarToken(tokenForA, userB);

        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("validarToken — malformed token returns false")
    void validarToken_tokenMalformado_retornaFalse() {
        UserDetails user = buildUserDetails("user@test.cl");

        boolean valid = jwtTokenProvider.validarToken("not.a.valid.jwt", user);

        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("obtenerExpiracion — returns configured expiration value")
    void obtenerExpiracion_retornaValorConfigurado() {
        assertThat(jwtTokenProvider.obtenerExpiracion()).isEqualTo(TEST_EXPIRATION);
    }

    // =========================================================
    // Helpers
    // =========================================================

    private UserDetails buildUserDetails(String email) {
        return new User(email, "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
