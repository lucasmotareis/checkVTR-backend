package pmto._bpm.viaturas.auth.passwordreset.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pmto._bpm.viaturas.auth.passwordreset.dto.ForgotPasswordRequest;
import pmto._bpm.viaturas.auth.passwordreset.dto.ResetPasswordRequest;
import pmto._bpm.viaturas.auth.passwordreset.model.PasswordResetToken;
import pmto._bpm.viaturas.auth.passwordreset.repository.PasswordResetTokenRepository;
import pmto._bpm.viaturas.users.model.User;
import pmto._bpm.viaturas.users.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class PasswordResetService {

    private static final Duration TOKEN_TTL = Duration.ofMinutes(15);
    private static final String INVALID_TOKEN_MESSAGE = "Invalid or expired reset token.";

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenDeliveryPort deliveryPort;
    private final PasswordResetTokenGenerator tokenGenerator;
    private final Clock clock;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            PasswordResetTokenDeliveryPort deliveryPort,
            PasswordResetTokenGenerator tokenGenerator,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.deliveryPort = deliveryPort;
        this.tokenGenerator = tokenGenerator;
        this.clock = clock;
    }

    @Transactional
    public void requestForgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmailIgnoreCase(request.getEmail())
                .filter(User::isEmailVerified)
                .ifPresent(user -> {
                    String rawToken = tokenGenerator.generate();
                    Instant expiresAt = Instant.now(clock).plus(TOKEN_TTL);

                    PasswordResetToken passwordResetToken = new PasswordResetToken();
                    passwordResetToken.setUser(user);
                    passwordResetToken.setTokenHash(sha256Hex(rawToken));
                    passwordResetToken.setExpiresAt(expiresAt);

                    passwordResetTokenRepository.save(passwordResetToken);
                    deliveryPort.deliver(user, rawToken, expiresAt);
                });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String tokenHash = sha256Hex(request.getToken());
        PasswordResetToken token = passwordResetTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException(INVALID_TOKEN_MESSAGE));

        Instant now = Instant.now(clock);
        if (token.getUsedAt() != null || !token.getExpiresAt().isAfter(now)) {
            throw new IllegalArgumentException(INVALID_TOKEN_MESSAGE);
        }

        User user = token.getUser();
        user.setSenha(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        token.setUsedAt(now);
        passwordResetTokenRepository.save(token);
    }

    private String sha256Hex(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
