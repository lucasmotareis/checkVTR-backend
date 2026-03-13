package pmto._bpm.viaturas.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import pmto._bpm.viaturas.auth.model.Role;
import pmto._bpm.viaturas.auth.service.JwtService;
import pmto._bpm.viaturas.users.model.User;
import pmto._bpm.viaturas.users.service.UserService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternalShouldAuthenticateUsingAuthorizationHeader() throws ServletException, IOException {
        JwtAuthFilter filter = new JwtAuthFilter(jwtService, userService);
        User user = buildUser("123456", Role.MOTORISTA);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/viaturas");
        request.addHeader("Authorization", "Bearer valid-token");

        when(jwtService.extractUsername("valid-token")).thenReturn("123456");
        when(userService.loadUserByUsername("123456")).thenReturn(user);
        when(jwtService.isTokenValid("valid-token", user)).thenReturn(true);

        filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
        assertEquals("123456", ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getMatricula());
        verify(filterChain).doFilter(eq(request), any());
    }

    @Test
    void doFilterInternalShouldAuthenticateUsingCookieWhenHeaderMissing() throws ServletException, IOException {
        JwtAuthFilter filter = new JwtAuthFilter(jwtService, userService);
        User user = buildUser("999999", Role.CHEFE_TRANSPORTE);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/notifications");
        request.setCookies(new Cookie("token", "cookie-token"));

        when(jwtService.extractUsername("cookie-token")).thenReturn("999999");
        when(userService.loadUserByUsername("999999")).thenReturn(user);
        when(jwtService.isTokenValid("cookie-token", user)).thenReturn(true);

        filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
        assertEquals("999999", ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getMatricula());
        verify(filterChain).doFilter(eq(request), any());
    }

    private User buildUser(String matricula, Role role) {
        User user = new User();
        user.setMatricula(matricula);
        user.setRole(role);
        return user;
    }
}
