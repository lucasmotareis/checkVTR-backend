package pmto._bpm.viaturas.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import pmto._bpm.viaturas.auth.config.JwtProperties;
import pmto._bpm.viaturas.auth.model.Role;
import pmto._bpm.viaturas.users.model.User;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "01234567890123456789012345678901";

    @Test
    void generateTokenShouldContainExpectedSubjectAndRoleClaim() {
        JwtService jwtService = buildService(SECRET);
        User user = buildUser("123456", Role.CHEFE_TRANSPORTE);

        String token = jwtService.generateToken(user);

        Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals("123456", claims.getSubject());
        assertEquals("CHEFE_TRANSPORTE", claims.get("role", String.class));
    }

    @Test
    void isTokenValidShouldReturnTrueForNonExpiredToken() {
        JwtService jwtService = buildService(SECRET);
        User user = buildUser("ABC123", Role.MOTORISTA);

        String token = jwtService.generateToken(user);

        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void shouldRejectMalformedOrExpiredToken() {
        JwtService jwtService = buildService(SECRET);
        User user = buildUser("999999", Role.MOTORISTA);

        assertThrows(Exception.class, () -> jwtService.extractUsername("token.invalido"));

        Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .setSubject("999999")
                .claim("role", "MOTORISTA")
                .setIssuedAt(new Date(System.currentTimeMillis() - 3600000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        assertThrows(Exception.class, () -> jwtService.isTokenValid(expiredToken, user));
    }

    private JwtService buildService(String secret) {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(secret);
        return new JwtService(properties);
    }

    private User buildUser(String matricula, Role role) {
        User user = new User();
        user.setMatricula(matricula);
        user.setRole(role);
        return user;
    }
}

