package pmto._bpm.viaturas.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pmto._bpm.viaturas.auth.dto.AuthResponse;
import pmto._bpm.viaturas.auth.dto.LoginRequest;
import pmto._bpm.viaturas.auth.dto.RegisterRequest;
import pmto._bpm.viaturas.auth.dto.UserResponse;
import pmto._bpm.viaturas.auth.service.AuthService;
import pmto._bpm.viaturas.users.model.User;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest dto,
                                   @RequestHeader(value = "X-Client-Type", defaultValue = "mobile") String clientType,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {

        AuthResponse token = authService.login(dto);

        if ("web".equalsIgnoreCase(clientType)) {
            boolean secureCookie = isSecureRequest(request);
            ResponseCookie cookie = ResponseCookie.from("token", token.getToken())
                    .httpOnly(true)
                    .secure(secureCookie)
                    .path("/")
                    .maxAge(Duration.ofHours(1))
                    .sameSite(secureCookie ? "None" : "Lax")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            return ResponseEntity.ok(Map.of("user", token.getUser()));
        }

        return ResponseEntity.ok(Map.of(
                "token", token.getToken(),
                "user", token.getUser()
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest dto) {
        authService.register(dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new UserResponse(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        boolean secureCookie = isSecureRequest(request);
        ResponseCookie cookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .sameSite(secureCookie ? "None" : "Lax")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok().build();
    }

    private boolean isSecureRequest(HttpServletRequest request) {
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        return request.isSecure() || "https".equalsIgnoreCase(forwardedProto);
    }
}
