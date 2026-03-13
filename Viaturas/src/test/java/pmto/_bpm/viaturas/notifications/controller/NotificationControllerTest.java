package pmto._bpm.viaturas.notifications.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import pmto._bpm.viaturas.auth.model.Role;
import pmto._bpm.viaturas.auth.security.JwtAuthFilter;
import pmto._bpm.viaturas.batalhao.model.Batalhao;
import pmto._bpm.viaturas.notifications.dto.NotificacaoItemDTO;
import pmto._bpm.viaturas.notifications.service.NotificationService;
import pmto._bpm.viaturas.testsupport.TestSecurityConfig;
import pmto._bpm.viaturas.users.model.User;
import pmto._bpm.viaturas.users.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import(TestSecurityConfig.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void allowFilterChain() throws Exception {
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    @Test
    void getNotificationsShouldReturnPageWithExpectedPayloadShape() throws Exception {
        NotificacaoItemDTO dto = new NotificacaoItemDTO(
                1L,
                "Alerta",
                "Mensagem",
                OffsetDateTime.now(),
                "8 BPM",
                false
        );

        when(notificationService.listaNotificacaoPaginado(any(User.class), any()))
                .thenReturn(new PageImpl<>(List.of(dto), PageRequest.of(0, 8), 1));

        mockMvc.perform(get("/notifications")
                        .param("page", "0")
                        .param("size", "8")
                        .with(authentication(auth(Role.MOTORISTA, 1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].titulo").value("Alerta"))
                .andExpect(jsonPath("$.content[0].nomeBatalhao").value("8 BPM"));
    }

    @Test
    void postNotificationShouldBeForbiddenForNonChief() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "titulo", "Aviso",
                "mensagem", "Mensagem de teste"
        ));

        mockMvc.perform(post("/notifications")
                        .with(authentication(auth(Role.MOTORISTA, 1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void postNotificationShouldReturnBadRequestWhenPayloadInvalid() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "titulo", "",
                "mensagem", ""
        ));

        mockMvc.perform(post("/notifications")
                        .with(authentication(auth(Role.CHEFE_TRANSPORTE, 1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(notificationService, never()).criar(any());
    }

    @Test
    void postMeVistoShouldReturnNoContent() throws Exception {
        User user = userWithRole(Role.MOTORISTA, 1L);
        when(userRepository.save(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/notifications/me/visto")
                        .with(authentication(auth(Role.MOTORISTA, 1L))))
                .andExpect(status().isNoContent());

        verify(userRepository).save(any(User.class));
    }

    private UsernamePasswordAuthenticationToken auth(Role role, Long batalhaoId) {
        User user = userWithRole(role, batalhaoId);
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private User userWithRole(Role role, Long batalhaoId) {
        Batalhao batalhao = new Batalhao();
        batalhao.setId(batalhaoId);
        batalhao.setNome("8 BPM");

        User user = new User();
        user.setNomeGuerra("Silva");
        user.setMatricula("123456");
        user.setRole(role);
        user.setBatalhao(batalhao);
        return user;
    }
}
