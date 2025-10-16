package pmto._bpm.Viaturas.auth.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pmto._bpm.Viaturas.auth.dto.JwtResponse;
import pmto._bpm.Viaturas.auth.dto.LoginRequest;
import pmto._bpm.Viaturas.auth.dto.RegisterRequest;
import pmto._bpm.Viaturas.auth.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest dto) {
        String token = authService.login(dto);
        return ResponseEntity.ok(new JwtResponse(token));

    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest dto) {
        authService.register(dto);
        return ResponseEntity.ok().build();
    }


}
