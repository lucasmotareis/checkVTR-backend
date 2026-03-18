package pmto._bpm.viaturas.auth.passwordreset.controller;

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
import pmto._bpm.viaturas.auth.passwordreset.dto.ForgotPasswordRequest;
import pmto._bpm.viaturas.auth.passwordreset.dto.ResetPasswordRequest;
import pmto._bpm.viaturas.auth.passwordreset.service.PasswordResetService;
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
        controllers = PasswordResetController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class PasswordResetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void forgotPasswordRequestShouldReturnGenericSuccessEvenIfUserDoesNotExist() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("email", "missing@pmto.gov.br"));

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", not(isEmptyOrNullString())));

        verify(passwordResetService).requestForgotPassword(any(ForgotPasswordRequest.class));
    }

    @Test
    void forgotPasswordShouldNotLeakWhetherAccountExists() throws Exception {
        String existingBody = objectMapper.writeValueAsString(Map.of("email", "existing@pmto.gov.br"));
        String missingBody = objectMapper.writeValueAsString(Map.of("email", "missing@pmto.gov.br"));

        String existingResponse = mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(existingBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String missingResponse = mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(missingBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(existingResponse, missingResponse);
        verify(passwordResetService, times(2)).requestForgotPassword(any(ForgotPasswordRequest.class));
    }

    @Test
    void resetPasswordShouldReturnBadRequestWhenTokenIsInvalid() throws Exception {
        doThrow(new IllegalArgumentException("Invalid reset token."))
                .when(passwordResetService)
                .resetPassword(any(ResetPasswordRequest.class));

        String body = objectMapper.writeValueAsString(Map.of(
                "token", "invalid-token",
                "newPassword", "NovaSenha123"
        ));

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid reset token."));
    }

    @Test
    void forgotPasswordShouldReturnBadRequestWhenPayloadIsMissingEmail() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of());

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(passwordResetService, never()).requestForgotPassword(any(ForgotPasswordRequest.class));
    }

    @Test
    void forgotPasswordShouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("email", "not-an-email"));

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(passwordResetService, never()).requestForgotPassword(any(ForgotPasswordRequest.class));
    }

    @Test
    void resetPasswordShouldReturnBadRequestWhenPayloadIsInvalid() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "token", "",
                "newPassword", ""
        ));

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(passwordResetService, never()).resetPassword(any(ResetPasswordRequest.class));
    }
}
