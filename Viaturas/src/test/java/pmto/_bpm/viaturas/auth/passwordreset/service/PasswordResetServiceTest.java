package pmto._bpm.viaturas.auth.passwordreset.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordResetTokenDeliveryPort deliveryPort;

    @Mock
    private PasswordResetTokenGenerator tokenGenerator;

    private PasswordResetService passwordResetService;

    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-03-17T12:00:00Z"), ZoneOffset.UTC);
        passwordResetService = new PasswordResetService(
                userRepository,
                passwordResetTokenRepository,
                passwordEncoder,
                deliveryPort,
                tokenGenerator,
                fixedClock
        );
    }

    @Test
    void forgotPasswordRequestShouldBeSilentWhenUserDoesNotExist() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("missing@pmto.gov.br");
        when(userRepository.findByEmailIgnoreCase("missing@pmto.gov.br")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> passwordResetService.requestForgotPassword(request));

        verify(passwordResetTokenRepository, never()).save(any(PasswordResetToken.class));
        verify(deliveryPort, never()).deliver(any(User.class), any(String.class), any(Instant.class));
    }

    @Test
    void forgotPasswordShouldCreateResetTokenForExistingEligibleUser() {
        User user = buildVerifiedUser("silva@pmto.gov.br", "old-password");
        when(userRepository.findByEmailIgnoreCase("silva@pmto.gov.br")).thenReturn(Optional.of(user));
        when(tokenGenerator.generate()).thenReturn("raw-reset-token");

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("silva@pmto.gov.br");

        passwordResetService.requestForgotPassword(request);

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());

        PasswordResetToken savedToken = tokenCaptor.getValue();
        assertEquals(user, savedToken.getUser());
        assertEquals(sha256Hex("raw-reset-token"), savedToken.getTokenHash());
        assertNotEquals("raw-reset-token", savedToken.getTokenHash());
        assertEquals(Instant.parse("2026-03-17T12:15:00Z"), savedToken.getExpiresAt());

        verify(deliveryPort).deliver(eq(user), eq("raw-reset-token"), eq(Instant.parse("2026-03-17T12:15:00Z")));
    }

    @Test
    void resetPasswordShouldSucceedWithValidNonExpiredUnusedToken() {
        User user = buildVerifiedUser("silva@pmto.gov.br", "old-password");
        PasswordResetToken token = buildToken(user, "valid-token", Instant.parse("2026-03-17T12:30:00Z"), null);

        when(passwordResetTokenRepository.findByTokenHash(sha256Hex("valid-token"))).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NovaSenha123")).thenReturn("encoded-password");

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("valid-token");
        request.setNewPassword("NovaSenha123");

        assertDoesNotThrow(() -> passwordResetService.resetPassword(request));
    }

    @Test
    void resetPasswordShouldFailWithInvalidToken() {
        when(passwordResetTokenRepository.findByTokenHash(sha256Hex("invalid-token"))).thenReturn(Optional.empty());

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("invalid-token");
        request.setNewPassword("NovaSenha123");

        assertThrows(IllegalArgumentException.class, () -> passwordResetService.resetPassword(request));
    }

    @Test
    void resetPasswordShouldFailWithExpiredToken() {
        User user = buildVerifiedUser("silva@pmto.gov.br", "old-password");
        PasswordResetToken token = buildToken(
                user,
                "expired-token",
                Instant.parse("2026-03-17T11:59:59Z"),
                null
        );

        when(passwordResetTokenRepository.findByTokenHash(sha256Hex("expired-token"))).thenReturn(Optional.of(token));

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("expired-token");
        request.setNewPassword("NovaSenha123");

        assertThrows(IllegalArgumentException.class, () -> passwordResetService.resetPassword(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetPasswordShouldFailWithAlreadyUsedToken() {
        User user = buildVerifiedUser("silva@pmto.gov.br", "old-password");
        PasswordResetToken token = buildToken(
                user,
                "used-token",
                Instant.parse("2026-03-17T12:30:00Z"),
                Instant.parse("2026-03-17T11:58:00Z")
        );

        when(passwordResetTokenRepository.findByTokenHash(sha256Hex("used-token"))).thenReturn(Optional.of(token));

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("used-token");
        request.setNewPassword("NovaSenha123");

        assertThrows(IllegalArgumentException.class, () -> passwordResetService.resetPassword(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetPasswordShouldEncodeNewPasswordWithPasswordEncoder() {
        User user = buildVerifiedUser("silva@pmto.gov.br", "old-password");
        PasswordResetToken token = buildToken(user, "valid-token", Instant.parse("2026-03-17T12:30:00Z"), null);

        when(passwordResetTokenRepository.findByTokenHash(sha256Hex("valid-token"))).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NovaSenha123")).thenReturn("encoded-password");

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("valid-token");
        request.setNewPassword("NovaSenha123");

        passwordResetService.resetPassword(request);

        verify(passwordEncoder).encode("NovaSenha123");
        assertEquals("encoded-password", user.getSenha());
        verify(userRepository).save(user);
    }

    @Test
    void resetPasswordShouldMarkTokenAsUsed() {
        User user = buildVerifiedUser("silva@pmto.gov.br", "old-password");
        PasswordResetToken token = buildToken(user, "valid-token", Instant.parse("2026-03-17T12:30:00Z"), null);

        when(passwordResetTokenRepository.findByTokenHash(sha256Hex("valid-token"))).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NovaSenha123")).thenReturn("encoded-password");

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("valid-token");
        request.setNewPassword("NovaSenha123");

        passwordResetService.resetPassword(request);

        assertNotNull(token.getUsedAt());
        assertEquals(Instant.parse("2026-03-17T12:00:00Z"), token.getUsedAt());
        verify(passwordResetTokenRepository).save(token);
    }

    private User buildVerifiedUser(String email, String senha) {
        User user = new User();
        user.setEmail(email);
        user.setEmailVerified(true);
        user.setSenha(senha);
        return user;
    }

    private PasswordResetToken buildToken(User user, String rawToken, Instant expiresAt, Instant usedAt) {
        PasswordResetToken token = new PasswordResetToken();
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
