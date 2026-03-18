package pmto._bpm.viaturas.auth.emailverification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pmto._bpm.viaturas.auth.emailverification.dto.ResendVerificationEmailRequest;
import pmto._bpm.viaturas.auth.emailverification.dto.VerifyEmailRequest;
import pmto._bpm.viaturas.auth.emailverification.model.EmailVerificationToken;
import pmto._bpm.viaturas.auth.emailverification.repository.EmailVerificationTokenRepository;
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
public class EmailVerificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailVerificationService.class);
    private static final Duration TOKEN_TTL = Duration.ofHours(24);
    private static final String INVALID_TOKEN_MESSAGE = "Invalid or expired verification token.";

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailVerificationTokenDeliveryPort deliveryPort;
    private final EmailVerificationTokenGenerator tokenGenerator;
    private final Clock clock;

    public EmailVerificationService(
            UserRepository userRepository,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            EmailVerificationTokenDeliveryPort deliveryPort,
            EmailVerificationTokenGenerator tokenGenerator,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.deliveryPort = deliveryPort;
        this.tokenGenerator = tokenGenerator;
        this.clock = clock;
    }

    @Transactional
    public void resendVerificationEmail(ResendVerificationEmailRequest request) {
        userRepository.findByEmailIgnoreCase(request.getEmail())
                .filter(user -> !user.isEmailVerified())
                .ifPresent(user -> {
                    String rawToken = tokenGenerator.generate();
                    Instant expiresAt = Instant.now(clock).plus(TOKEN_TTL);

                    EmailVerificationToken token = new EmailVerificationToken();
                    token.setUser(user);
                    token.setTokenHash(sha256Hex(rawToken));
                    token.setExpiresAt(expiresAt);

                    EmailVerificationToken savedToken = emailVerificationTokenRepository.save(token);
                    try {
                        deliveryPort.deliver(user, rawToken, expiresAt);
                    } catch (RuntimeException deliveryError) {
                        // Keep endpoint generic and cleanup token if delivery fails.
                        try {
                            emailVerificationTokenRepository.delete(savedToken);
                        } catch (RuntimeException cleanupError) {
                            LOGGER.warn("Failed to cleanup email verification token for userId={}.",
                                    user.getId(), cleanupError);
                        }
                        LOGGER.warn("Failed to deliver verification email for userId={}.", user.getId(), deliveryError);
                    }
                });
    }

    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        String tokenHash = sha256Hex(request.getToken());
        EmailVerificationToken token = emailVerificationTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException(INVALID_TOKEN_MESSAGE));

        Instant now = Instant.now(clock);
        if (token.getUsedAt() != null || !token.getExpiresAt().isAfter(now)) {
            throw new IllegalArgumentException(INVALID_TOKEN_MESSAGE);
        }

        User user = token.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        token.setUsedAt(now);
        emailVerificationTokenRepository.save(token);
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
