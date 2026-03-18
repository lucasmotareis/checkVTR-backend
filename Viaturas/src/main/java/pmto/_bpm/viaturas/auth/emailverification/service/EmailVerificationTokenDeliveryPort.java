package pmto._bpm.viaturas.auth.emailverification.service;

import pmto._bpm.viaturas.users.model.User;

import java.time.Instant;

public interface EmailVerificationTokenDeliveryPort {

    void deliver(User user, String rawToken, Instant expiresAt);
}
