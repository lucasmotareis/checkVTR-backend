package pmto._bpm.viaturas.auth.passwordreset.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import pmto._bpm.viaturas.auth.passwordreset.config.PasswordResetMailProperties;
import pmto._bpm.viaturas.users.model.User;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JavaMailPasswordResetTokenDeliveryAdapterTest {

    @Mock
    private JavaMailSender mailSender;

    private PasswordResetMailProperties properties;
    private JavaMailPasswordResetTokenDeliveryAdapter adapter;

    @BeforeEach
    void setUp() {
        properties = new PasswordResetMailProperties();
        properties.setResetUrlBase("https://app.docheck.com/reset-password");
        properties.setFromEmail("no-reply@docheck.com");
        adapter = new JavaMailPasswordResetTokenDeliveryAdapter(mailSender, properties);
    }

    @Test
    void deliverShouldSendMessageWithConfiguredResetUrl() {
        User user = new User();
        user.setEmail("silva@pmto.gov.br");

        adapter.deliver(user, "abc123token", Instant.parse("2026-03-18T15:00:00Z"));

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertEquals("no-reply@docheck.com", sent.getFrom());
        assertEquals("silva@pmto.gov.br", sent.getTo()[0]);
        assertEquals("Recuperacao de senha", sent.getSubject());
        assertTrue(sent.getText().contains("https://app.docheck.com/reset-password?token=abc123token"));
    }

    @Test
    void deliverShouldAppendTokenWhenBaseUrlAlreadyHasQueryParamsAndEncodeToken() {
        properties.setResetUrlBase("https://app.docheck.com/reset-password?source=email");

        User user = new User();
        user.setEmail("silva@pmto.gov.br");

        adapter.deliver(user, "a+b/c==", Instant.parse("2026-03-18T15:00:00Z"));

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        String body = captor.getValue().getText();
        int urlStart = body.indexOf("https://app.docheck.com/reset-password");
        int urlEnd = body.indexOf('\n', urlStart);
        String url = body.substring(urlStart, urlEnd > 0 ? urlEnd : body.length()).trim();

        var queryParams = UriComponentsBuilder
                .fromUri(URI.create(url))
                .build()
                .getQueryParams();

        assertEquals("email", queryParams.getFirst("source"));
        assertEquals("a+b/c==",
                URLDecoder.decode(queryParams.getFirst("token"), StandardCharsets.UTF_8));
    }

    @Test
    void deliverShouldNotSendWhenUserEmailIsMissing() {
        User user = new User();
        user.setEmail(" ");

        adapter.deliver(user, "abc123token", Instant.parse("2026-03-18T15:00:00Z"));

        verify(mailSender, never()).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
    }
}
