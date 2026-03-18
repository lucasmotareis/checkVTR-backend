package pmto._bpm.viaturas.auth.passwordreset.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pmto._bpm.viaturas.auth.passwordreset.service.PasswordResetTokenDeliveryPort;
import pmto._bpm.viaturas.users.repository.UserRepository;

@Component
public class PasswordResetEmailRequestedEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetEmailRequestedEventListener.class);

    private final UserRepository userRepository;
    private final PasswordResetTokenDeliveryPort deliveryPort;

    public PasswordResetEmailRequestedEventListener(
            UserRepository userRepository,
            PasswordResetTokenDeliveryPort deliveryPort
    ) {
        this.userRepository = userRepository;
        this.deliveryPort = deliveryPort;
    }

    @Async("mailDispatchExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPasswordResetEmailRequested(PasswordResetEmailRequestedEvent event) {
        userRepository.findById(event.userId()).ifPresent(user -> {
            try {
                deliveryPort.deliver(user, event.rawToken(), event.expiresAt());
            } catch (RuntimeException deliveryError) {
                LOGGER.warn("Failed to deliver password reset email for userId={}.", user.getId(), deliveryError);
            }
        });
    }
}
