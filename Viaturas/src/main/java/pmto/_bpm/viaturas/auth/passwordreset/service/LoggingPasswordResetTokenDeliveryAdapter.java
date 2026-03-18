package pmto._bpm.viaturas.auth.passwordreset.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pmto._bpm.viaturas.users.model.User;

import java.time.Instant;

@Component
public class LoggingPasswordResetTokenDeliveryAdapter implements PasswordResetTokenDeliveryPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingPasswordResetTokenDeliveryAdapter.class);

    @Override
    public void deliver(User user, String rawToken, Instant expiresAt) {
        LOGGER.info(
                "Password reset requested for userId={} email={} token={} expiresAt={}",
                user.getId(),
                user.getEmail(),
                rawToken,
                expiresAt
        );
    }
}
