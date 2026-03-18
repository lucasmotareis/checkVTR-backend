package pmto._bpm.viaturas.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pmto._bpm.viaturas.auth.dto.AuthResponse;
import pmto._bpm.viaturas.auth.dto.LoginRequest;
import pmto._bpm.viaturas.auth.dto.RegisterRequest;
import pmto._bpm.viaturas.auth.dto.UserResponse;
import pmto._bpm.viaturas.auth.model.Role;
import pmto._bpm.viaturas.auth.security.JwtAuthFilter;
import pmto._bpm.viaturas.auth.service.AuthService;
import pmto._bpm.viaturas.batalhao.model.Batalhao;
import pmto._bpm.viaturas.users.model.User;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void loginShouldReturnTokenInBodyForMobileClient() throws Exception {
        AuthResponse authResponse = new AuthResponse("jwt-token", buildUserResponse());
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        String body = objectMapper.writeValueAsString(Map.of(
                "matricula", "123456",
                "senha", "abc123"
        ));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Client-Type", "mobile")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.user.matricula").value("123456"));
    }

    @Test
    void loginShouldSetCookieForWebClient() throws Exception {
        AuthResponse authResponse = new AuthResponse("jwt-token", buildUserResponse());
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        String body = objectMapper.writeValueAsString(Map.of(
                "matricula", "123456",
                "senha", "abc123"
        ));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Client-Type", "web")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("token=jwt-token")))
                .andExpect(jsonPath("$.user.matricula").value("123456"))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void registerShouldReturnOk() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn("ok");

        String body = objectMapper.writeValueAsString(Map.of(
                "nomeGuerra", "Silva",
                "graduacao", "SGT",
                "senha", "abc123",
                "cpf", "12345678900",
                "matricula", "123456",
                "batalhaoId", 1
        ));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(authService).register(any(RegisterRequest.class));
    }

    private UserResponse buildUserResponse() {
        Batalhao batalhao = new Batalhao();
        batalhao.setId(1L);
        batalhao.setNome("8 BPM");

        User user = new User();
        user.setNomeGuerra("Silva");
        user.setMatricula("123456");
        user.setRole(Role.MOTORISTA);
        user.setBatalhao(batalhao);

        return new UserResponse(user);
    }
}
