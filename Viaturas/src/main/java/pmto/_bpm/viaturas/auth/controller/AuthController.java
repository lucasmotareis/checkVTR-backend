package pmto._bpm.viaturas.auth.controller;


import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pmto._bpm.viaturas.auth.dto.AuthResponse;
import pmto._bpm.viaturas.auth.dto.LoginRequest;
import pmto._bpm.viaturas.auth.dto.RegisterRequest;
import pmto._bpm.viaturas.auth.dto.UserResponse;
import pmto._bpm.viaturas.auth.model.User;
import pmto._bpm.viaturas.auth.service.AuthService;

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
                                   HttpServletResponse response) {


        AuthResponse token = authService.login(dto);

        if (!"CHEFE_TRANSPORTE".equals(token.getUser().getRole()) && "web".equalsIgnoreCase(clientType)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso restrito a chefes de transporte.");
        }


        if ("web".equalsIgnoreCase(clientType)) {
            ResponseCookie cookie = ResponseCookie.from("token", token.getToken())
                    .httpOnly(true)
                    .secure(true) // use true em produção com HTTPS
                    .path("/")
                    .maxAge(Duration.ofHours(1))
                    .sameSite("None")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            return ResponseEntity.ok(Map.of("user", token.getUser()));
        }

        // Se for mobile, retornar no body
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
        User user = (User) authentication.getPrincipal(); // ou via SecurityContextHolder
        return ResponseEntity.ok(new UserResponse(user)); // Retorna apenas dados seguros
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(0)  // Remove o cookie
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok().build();
    }

}
