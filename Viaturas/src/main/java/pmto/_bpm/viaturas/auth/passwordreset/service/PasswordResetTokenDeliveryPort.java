package pmto._bpm.viaturas.auth.passwordreset.service;

import pmto._bpm.viaturas.users.model.User;

import java.time.Instant;

public interface PasswordResetTokenDeliveryPort {

    void deliver(User user, String rawToken, Instant expiresAt);
}
