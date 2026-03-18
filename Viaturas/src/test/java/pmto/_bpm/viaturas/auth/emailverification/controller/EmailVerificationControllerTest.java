package pmto._bpm.viaturas.auth.emailverification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pmto._bpm.viaturas.auth.emailverification.dto.ResendVerificationEmailRequest;
import pmto._bpm.viaturas.auth.emailverification.dto.VerifyEmailRequest;
import pmto._bpm.viaturas.auth.emailverification.service.EmailVerificationService;
import pmto._bpm.viaturas.auth.security.JwtAuthFilter;

import java.util.Map;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = EmailVerificationController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class EmailVerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmailVerificationService emailVerificationService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void verifyEmailShouldReturnOkForValidPayload() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("token", "valid-token"));

        mockMvc.perform(post("/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", not(isEmptyOrNullString())));

        verify(emailVerificationService).verifyEmail(any(VerifyEmailRequest.class));
    }

    @Test
    void verifyEmailShouldReturnBadRequestWhenTokenIsInvalid() throws Exception {
        doThrow(new IllegalArgumentException("Invalid or expired verification token."))
                .when(emailVerificationService)
                .verifyEmail(any(VerifyEmailRequest.class));

        String body = objectMapper.writeValueAsString(Map.of("token", "invalid-token"));

        mockMvc.perform(post("/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired verification token."));
    }

    @Test
    void resendVerificationShouldReturnGenericSuccessAndNotLeakAccountExistence() throws Exception {
        String existingBody = objectMapper.writeValueAsString(Map.of("email", "existing@pmto.gov.br"));
        String missingBody = objectMapper.writeValueAsString(Map.of("email", "missing@pmto.gov.br"));

        String existingResponse = mockMvc.perform(post("/auth/resend-verification-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(existingBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String missingResponse = mockMvc.perform(post("/auth/resend-verification-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(missingBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(existingResponse, missingResponse);
        verify(emailVerificationService, times(2)).resendVerificationEmail(any(ResendVerificationEmailRequest.class));
    }

    @Test
    void verifyEmailShouldReturnBadRequestWhenPayloadIsMissingToken() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of());

        mockMvc.perform(post("/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(emailVerificationService, never()).verifyEmail(any(VerifyEmailRequest.class));
    }

    @Test
    void resendVerificationShouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("email", "invalid-email"));

        mockMvc.perform(post("/auth/resend-verification-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(emailVerificationService, never()).resendVerificationEmail(any(ResendVerificationEmailRequest.class));
    }
}
