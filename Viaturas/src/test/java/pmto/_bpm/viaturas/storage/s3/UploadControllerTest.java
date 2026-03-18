package pmto._bpm.viaturas.storage.s3;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import pmto._bpm.viaturas.auth.model.Role;
import pmto._bpm.viaturas.auth.security.JwtAuthFilter;
import pmto._bpm.viaturas.batalhao.model.Batalhao;
import pmto._bpm.viaturas.testsupport.TestSecurityConfig;
import pmto._bpm.viaturas.users.model.User;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UploadController.class)
@Import(TestSecurityConfig.class)
class UploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AwsS3Service awsS3Service;

    @MockitoBean
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
    void generateUploadUrlShouldReturnPresignedUrls() throws Exception {
        when(awsS3Service.generatePresignedUrls(any()))
                .thenReturn(List.of("https://s3.url/1", "https://s3.url/2"));

        String body = objectMapper.writeValueAsString(Map.of(
                "quantidade", 2,
                "metadata", Map.of(
                        "batalhao", "8-bpm",
                        "prefixo", "PM-10",
                        "placa", "ABC1234",
                        "matricula", "123456"
                )
        ));

        mockMvc.perform(post("/upload/presigned-urls")
                        .with(authentication(auth(Role.MOTORISTA, 1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("https://s3.url/1"))
                .andExpect(jsonPath("$[1]").value("https://s3.url/2"));
    }

    @Test
    void generateUploadUrlShouldRequireAuthentication() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "quantidade", 1,
                "metadata", Map.of(
                        "batalhao", "8-bpm",
                        "prefixo", "PM-10",
                        "placa", "ABC1234",
                        "matricula", "123456"
                )
        ));

        mockMvc.perform(post("/upload/presigned-urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    private UsernamePasswordAuthenticationToken auth(Role role, Long batalhaoId) {
        Batalhao batalhao = new Batalhao();
        batalhao.setId(batalhaoId);
        batalhao.setNome("8 BPM");

        User user = new User();
        user.setNomeGuerra("Silva");
        user.setMatricula("123456");
        user.setRole(role);
        user.setBatalhao(batalhao);

        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }
}
