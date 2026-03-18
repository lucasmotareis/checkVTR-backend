package pmto._bpm.viaturas.auth.passwordreset.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import pmto._bpm.viaturas.auth.passwordreset.config.PasswordResetMailProperties;
import pmto._bpm.viaturas.users.model.User;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
public class JavaMailPasswordResetTokenDeliveryAdapter implements PasswordResetTokenDeliveryPort {

    private final JavaMailSender mailSender;
    private final PasswordResetMailProperties properties;

    public JavaMailPasswordResetTokenDeliveryAdapter(
            JavaMailSender mailSender,
            PasswordResetMailProperties properties
    ) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    public void deliver(User user, String rawToken, Instant expiresAt) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }

        String baseUrl = properties.getResetUrlBase().trim();
        String encodedToken = URLEncoder.encode(rawToken, StandardCharsets.UTF_8)
                .replace("+", "%20");
        String separator = baseUrl.contains("?") ? "&" : "?";
        String resetUrl = baseUrl + separator + "token=" + encodedToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getFromEmail().trim());
        message.setTo(user.getEmail().trim());
        message.setSubject("Recuperacao de senha");
        message.setText(
                "Recebemos uma solicitacao para redefinir sua senha.\n\n" +
                        "Use o link abaixo para criar uma nova senha:\n" +
                        resetUrl + "\n\n" +
                        "Este link expira em: " + expiresAt + "\n\n" +
                        "Se voce nao solicitou essa alteracao, ignore este email."
        );

        mailSender.send(message);
    }
}
