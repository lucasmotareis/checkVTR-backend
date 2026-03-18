package pmto._bpm.viaturas.auth.passwordreset.event;

import java.time.Instant;

public record PasswordResetEmailRequestedEvent(
        Long userId,
        String rawToken,
        Instant expiresAt
) {
}
