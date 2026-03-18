package pmto._bpm.viaturas.auth.passwordreset.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pmto._bpm.viaturas.auth.passwordreset.dto.ForgotPasswordRequest;
import pmto._bpm.viaturas.auth.passwordreset.dto.PasswordResetResponse;
import pmto._bpm.viaturas.auth.passwordreset.dto.ResetPasswordRequest;
import pmto._bpm.viaturas.auth.passwordreset.service.PasswordResetService;

@RestController
@RequestMapping("/auth")
public class PasswordResetController {

    private static final String FORGOT_PASSWORD_GENERIC_MESSAGE =
            "Se a conta existe, instruções de reset foram enviadas.";
    private static final String RESET_PASSWORD_SUCCESS_MESSAGE =
            "Senha resetada com sucesso!";

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<PasswordResetResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestForgotPassword(request);
        return ResponseEntity.ok(new PasswordResetResponse(FORGOT_PASSWORD_GENERIC_MESSAGE));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<PasswordResetResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(new PasswordResetResponse(RESET_PASSWORD_SUCCESS_MESSAGE));
    }
}
