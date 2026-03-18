package pmto._bpm.viaturas.auth.emailverification.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import pmto._bpm.viaturas.auth.emailverification.config.EmailVerificationMailProperties;
import pmto._bpm.viaturas.users.model.User;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
public class JavaMailEmailVerificationTokenDeliveryAdapter implements EmailVerificationTokenDeliveryPort {

    private final JavaMailSender mailSender;
    private final EmailVerificationMailProperties properties;

    public JavaMailEmailVerificationTokenDeliveryAdapter(
            JavaMailSender mailSender,
            EmailVerificationMailProperties properties
    ) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    public void deliver(User user, String rawToken, Instant expiresAt) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }

        String baseUrl = properties.getVerifyUrlBase().trim();
        String encodedToken = URLEncoder.encode(rawToken, StandardCharsets.UTF_8)
                .replace("+", "%20");
        String separator = baseUrl.contains("?") ? "&" : "?";
        String verifyUrl = baseUrl + separator + "token=" + encodedToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getFromEmail().trim());
        message.setTo(user.getEmail().trim());
        message.setSubject("Verificacao de email");
        message.setText(
                "Confirme seu email para ativar sua conta.\n\n" +
                        "Use o link abaixo:\n" +
                        verifyUrl + "\n\n" +
                        "Este link expira em: " + expiresAt + "\n\n" +
                        "Se voce nao solicitou este cadastro, ignore este email."
        );

        mailSender.send(message);
    }
}
