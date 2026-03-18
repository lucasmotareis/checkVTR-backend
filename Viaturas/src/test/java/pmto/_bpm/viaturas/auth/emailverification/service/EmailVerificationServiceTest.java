package pmto._bpm.viaturas.auth.emailverification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Mock
    private EmailVerificationTokenDeliveryPort deliveryPort;

    @Mock
    private EmailVerificationTokenGenerator tokenGenerator;

    private EmailVerificationService emailVerificationService;

    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-03-18T12:00:00Z"), ZoneOffset.UTC);
        emailVerificationService = new EmailVerificationService(
                userRepository,
                emailVerificationTokenRepository,
                deliveryPort,
                tokenGenerator,
                fixedClock
        );
    }

    @Test
    void verifyEmailShouldSucceedWithValidToken() {
        User user = buildUnverifiedUser("silva@pmto.gov.br");
        EmailVerificationToken token = buildToken(user, "valid-token", Instant.parse("2026-03-18T12:30:00Z"), null);
        when(emailVerificationTokenRepository.findByTokenHash(sha256Hex("valid-token"))).thenReturn(Optional.of(token));

        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setToken("valid-token");

        assertDoesNotThrow(() -> emailVerificationService.verifyEmail(request));
    }

    @Test
    void verifyEmailShouldFailWithInvalidToken() {
        when(emailVerificationTokenRepository.findByTokenHash(sha256Hex("invalid-token"))).thenReturn(Optional.empty());

        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setToken("invalid-token");

        assertThrows(IllegalArgumentException.class, () -> emailVerificationService.verifyEmail(request));
    }

    @Test
    void verifyEmailShouldFailWithExpiredToken() {
        User user = buildUnverifiedUser("silva@pmto.gov.br");
        EmailVerificationToken token = buildToken(
                user,
                "expired-token",
                Instant.parse("2026-03-18T11:59:59Z"),
                null
        );
        when(emailVerificationTokenRepository.findByTokenHash(sha256Hex("expired-token"))).thenReturn(Optional.of(token));

        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setToken("expired-token");

        assertThrows(IllegalArgumentException.class, () -> emailVerificationService.verifyEmail(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void verifyEmailShouldFailWithAlreadyUsedToken() {
        User user = buildUnverifiedUser("silva@pmto.gov.br");
        EmailVerificationToken token = buildToken(
                user,
                "used-token",
                Instant.parse("2026-03-18T12:30:00Z"),
                Instant.parse("2026-03-18T11:58:00Z")
        );
        when(emailVerificationTokenRepository.findByTokenHash(sha256Hex("used-token"))).thenReturn(Optional.of(token));

        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setToken("used-token");

        assertThrows(IllegalArgumentException.class, () -> emailVerificationService.verifyEmail(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void verifyEmailShouldMarkUserAsVerified() {
        User user = buildUnverifiedUser("silva@pmto.gov.br");
        EmailVerificationToken token = buildToken(user, "valid-token", Instant.parse("2026-03-18T12:30:00Z"), null);
        when(emailVerificationTokenRepository.findByTokenHash(sha256Hex("valid-token"))).thenReturn(Optional.of(token));

        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setToken("valid-token");

        emailVerificationService.verifyEmail(request);

        assertEquals(true, user.isEmailVerified());
        verify(userRepository).save(user);
    }

    @Test
    void verifyEmailShouldMarkTokenAsUsed() {
        User user = buildUnverifiedUser("silva@pmto.gov.br");
        EmailVerificationToken token = buildToken(user, "valid-token", Instant.parse("2026-03-18T12:30:00Z"), null);
        when(emailVerificationTokenRepository.findByTokenHash(sha256Hex("valid-token"))).thenReturn(Optional.of(token));

        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setToken("valid-token");

        emailVerificationService.verifyEmail(request);

        assertNotNull(token.getUsedAt());
        assertEquals(Instant.parse("2026-03-18T12:00:00Z"), token.getUsedAt());
        verify(emailVerificationTokenRepository).save(token);
    }

    @Test
    void resendVerificationEmailShouldCreateAndSendTokenForEligibleUser() {
        User user = buildUnverifiedUser("silva@pmto.gov.br");
        when(userRepository.findByEmailIgnoreCase("silva@pmto.gov.br")).thenReturn(Optional.of(user));
        when(tokenGenerator.generate()).thenReturn("raw-verification-token");
        when(emailVerificationTokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResendVerificationEmailRequest request = new ResendVerificationEmailRequest();
        request.setEmail("silva@pmto.gov.br");

        emailVerificationService.resendVerificationEmail(request);

        ArgumentCaptor<EmailVerificationToken> tokenCaptor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(emailVerificationTokenRepository).save(tokenCaptor.capture());

        EmailVerificationToken savedToken = tokenCaptor.getValue();
        assertEquals(user, savedToken.getUser());
        assertEquals(sha256Hex("raw-verification-token"), savedToken.getTokenHash());
        assertNotEquals("raw-verification-token", savedToken.getTokenHash());
        assertEquals(Instant.parse("2026-03-19T12:00:00Z"), savedToken.getExpiresAt());

        verify(deliveryPort).deliver(eq(user), eq("raw-verification-token"), eq(Instant.parse("2026-03-19T12:00:00Z")));
    }

    @Test
    void resendVerificationEmailShouldDoNothingHarmfulForAlreadyVerifiedUser() {
        User user = new User();
        user.setEmail("verified@pmto.gov.br");
        user.setEmailVerified(true);
        when(userRepository.findByEmailIgnoreCase("verified@pmto.gov.br")).thenReturn(Optional.of(user));

        ResendVerificationEmailRequest request = new ResendVerificationEmailRequest();
        request.setEmail("verified@pmto.gov.br");

        assertDoesNotThrow(() -> emailVerificationService.resendVerificationEmail(request));

        verify(emailVerificationTokenRepository, never()).save(any(EmailVerificationToken.class));
        verify(deliveryPort, never()).deliver(any(User.class), any(String.class), any(Instant.class));
    }

    private User buildUnverifiedUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setEmailVerified(false);
        return user;
    }

    private EmailVerificationToken buildToken(User user, String rawToken, Instant expiresAt, Instant usedAt) {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setTokenHash(sha256Hex(rawToken));
        token.setExpiresAt(expiresAt);
        token.setUsedAt(usedAt);
        return token;
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
