package pmto._bpm.viaturas.auth.async;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;
import pmto._bpm.viaturas.auth.dto.RegisterRequest;
import pmto._bpm.viaturas.auth.model.CadastroAutorizado;
import pmto._bpm.viaturas.auth.passwordreset.dto.ForgotPasswordRequest;
import pmto._bpm.viaturas.auth.passwordreset.repository.PasswordResetTokenRepository;
import pmto._bpm.viaturas.auth.passwordreset.service.PasswordResetService;
import pmto._bpm.viaturas.auth.passwordreset.service.PasswordResetTokenDeliveryPort;
import pmto._bpm.viaturas.auth.passwordreset.service.PasswordResetTokenGenerator;
import pmto._bpm.viaturas.auth.service.AuthService;
import pmto._bpm.viaturas.batalhao.model.Batalhao;
import pmto._bpm.viaturas.batalhao.repository.BatalhaoRepository;
import pmto._bpm.viaturas.users.model.User;
import pmto._bpm.viaturas.users.repository.UserRepository;
import pmto._bpm.viaturas.auth.repository.CadastroAutorizadoRepository;
import pmto._bpm.viaturas.auth.emailverification.repository.EmailVerificationTokenRepository;
import pmto._bpm.viaturas.auth.emailverification.service.EmailVerificationTokenDeliveryPort;
import pmto._bpm.viaturas.auth.emailverification.service.EmailVerificationTokenGenerator;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=abcdefghijklmnopqrstuvwxyz012345"
})
class EmailDispatchTransactionIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BatalhaoRepository batalhaoRepository;

    @Autowired
    private CadastroAutorizadoRepository cadastroAutorizadoRepository;

    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @MockitoBean
    private EmailVerificationTokenDeliveryPort emailVerificationTokenDeliveryPort;

    @MockitoBean
    private PasswordResetTokenDeliveryPort passwordResetTokenDeliveryPort;

    @MockitoBean
    private EmailVerificationTokenGenerator emailVerificationTokenGenerator;

    @MockitoBean
    private PasswordResetTokenGenerator passwordResetTokenGenerator;

    @BeforeEach
    void setUp() {
        passwordResetTokenRepository.deleteAll();
        emailVerificationTokenRepository.deleteAll();
        userRepository.deleteAll();
        cadastroAutorizadoRepository.deleteAll();
        batalhaoRepository.deleteAll();

        reset(emailVerificationTokenDeliveryPort, passwordResetTokenDeliveryPort,
                emailVerificationTokenGenerator, passwordResetTokenGenerator);
    }

    @Test
    void registrationShouldDispatchVerificationEmailOnlyAfterCommit() {
        when(emailVerificationTokenGenerator.generate()).thenReturn("verification-token-commit");

        RegisterRequest request = buildRegisterRequest("12345678900", "123456", "silva@pmto.gov.br");
        saveRegistrationPrerequisites(request);

        transactionTemplate.executeWithoutResult(status -> {
            authService.register(request);
            verifyNoInteractions(emailVerificationTokenDeliveryPort);
        });

        verify(emailVerificationTokenDeliveryPort, timeout(1000).times(1))
                .deliver(any(User.class), eq("verification-token-commit"), any(Instant.class));
    }

    @Test
    void registrationShouldNotDispatchVerificationEmailWhenTransactionRollsBack() {
        when(emailVerificationTokenGenerator.generate()).thenReturn("verification-token-rollback");

        RegisterRequest request = buildRegisterRequest("22345678900", "223456", "rollback@pmto.gov.br");
        saveRegistrationPrerequisites(request);

        transactionTemplate.executeWithoutResult(status -> {
            authService.register(request);
            status.setRollbackOnly();
        });

        verify(emailVerificationTokenDeliveryPort, after(300).never())
                .deliver(any(User.class), any(String.class), any(Instant.class));
    }

    @Test
    void forgotPasswordShouldDispatchResetEmailOnlyAfterCommit() {
        when(passwordResetTokenGenerator.generate()).thenReturn("reset-token-commit");
        saveVerifiedUser("silva@pmto.gov.br");

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("silva@pmto.gov.br");

        transactionTemplate.executeWithoutResult(status -> {
            passwordResetService.requestForgotPassword(request);
            verifyNoInteractions(passwordResetTokenDeliveryPort);
        });

        verify(passwordResetTokenDeliveryPort, timeout(1000).times(1))
                .deliver(any(User.class), eq("reset-token-commit"), any(Instant.class));
    }

    @Test
    void forgotPasswordShouldNotDispatchResetEmailWhenTransactionRollsBack() {
        when(passwordResetTokenGenerator.generate()).thenReturn("reset-token-rollback");
        saveVerifiedUser("rollback@pmto.gov.br");

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("rollback@pmto.gov.br");

        transactionTemplate.executeWithoutResult(status -> {
            passwordResetService.requestForgotPassword(request);
            status.setRollbackOnly();
        });

        verify(passwordResetTokenDeliveryPort, after(300).never())
                .deliver(any(User.class), any(String.class), any(Instant.class));
    }

    private RegisterRequest buildRegisterRequest(String cpf, String matricula, String email) {
        RegisterRequest request = new RegisterRequest();
        request.setNomeGuerra("Silva");
        request.setGraduacao("SGT");
        request.setSenha("abc123");
        request.setCpf(cpf);
        request.setMatricula(matricula);
        request.setEmail(email);
        request.setBatalhaoId(1L);
        return request;
    }

    private void saveRegistrationPrerequisites(RegisterRequest request) {
        Batalhao batalhao = new Batalhao();
        batalhao.setNome("8 BPM");
        Batalhao savedBatalhao = batalhaoRepository.save(batalhao);
        request.setBatalhaoId(savedBatalhao.getId());

        CadastroAutorizado autorizado = new CadastroAutorizado();
        autorizado.setCpf(request.getCpf());
        autorizado.setMatricula(request.getMatricula());
        cadastroAutorizadoRepository.save(autorizado);
    }

    private void saveVerifiedUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setEmailVerified(true);
        user.setSenha("encoded");
        userRepository.save(user);
    }
}
