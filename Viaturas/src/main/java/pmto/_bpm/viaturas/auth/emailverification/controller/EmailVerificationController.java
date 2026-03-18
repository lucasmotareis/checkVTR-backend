package pmto._bpm.viaturas.auth.emailverification.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pmto._bpm.viaturas.auth.emailverification.dto.EmailVerificationResponse;
import pmto._bpm.viaturas.auth.emailverification.dto.ResendVerificationEmailRequest;
import pmto._bpm.viaturas.auth.emailverification.dto.VerifyEmailRequest;
import pmto._bpm.viaturas.auth.emailverification.service.EmailVerificationService;

@RestController
@RequestMapping("/auth")
public class EmailVerificationController {

    private static final String VERIFY_SUCCESS_MESSAGE = "Email verificado com sucesso!";
    private static final String RESEND_GENERIC_MESSAGE =
            "Se a conta existe e ainda nao foi verificada, enviaremos um novo email de verificacao.";

    private final EmailVerificationService emailVerificationService;

    public EmailVerificationController(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/verify-email")
    public ResponseEntity<EmailVerificationResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        emailVerificationService.verifyEmail(request);
        return ResponseEntity.ok(new EmailVerificationResponse(VERIFY_SUCCESS_MESSAGE));
    }

    @PostMapping("/resend-verification-email")
    public ResponseEntity<EmailVerificationResponse> resendVerificationEmail(
            @Valid @RequestBody ResendVerificationEmailRequest request
    ) {
        emailVerificationService.resendVerificationEmail(request);
        return ResponseEntity.ok(new EmailVerificationResponse(RESEND_GENERIC_MESSAGE));
    }
}
