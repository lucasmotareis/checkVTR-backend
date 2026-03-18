package pmto._bpm.viaturas.auth.emailverification.event;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pmto._bpm.viaturas.auth.emailverification.dto.ResendVerificationEmailRequest;
import pmto._bpm.viaturas.auth.emailverification.service.EmailVerificationService;

@Component
public class VerificationEmailRequestedEventListener {

    private final EmailVerificationService emailVerificationService;

    public VerificationEmailRequestedEventListener(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    @Async("mailDispatchExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVerificationEmailRequested(VerificationEmailRequestedEvent event) {
        ResendVerificationEmailRequest request = new ResendVerificationEmailRequest();
        request.setEmail(event.email());
        emailVerificationService.resendVerificationEmail(request);
    }
}
