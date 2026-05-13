package pmto._bpm.viaturas.viaturas.controller;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pmto._bpm.viaturas.auth.model.Role;
import pmto._bpm.viaturas.auth.security.JwtAuthFilter;
import pmto._bpm.viaturas.batalhao.model.Batalhao;
import pmto._bpm.viaturas.testsupport.TestSecurityConfig;
import pmto._bpm.viaturas.users.model.User;
import pmto._bpm.viaturas.viaturas.dto.ViaturaByIdDTO;
import pmto._bpm.viaturas.viaturas.dto.ViaturaDTO;
import pmto._bpm.viaturas.viaturas.model.Viatura;
import pmto._bpm.viaturas.viaturas.service.ViaturaService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ViaturaController.class)
@Import(TestSecurityConfig.class)
class ViaturaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ViaturaService viaturaService;

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
    void getViaturasShouldReturnBattalionScopedList() throws Exception {
        ViaturaByIdDTO dto = new ViaturaByIdDTO(
                1L, "PM-01", "ABC-1234", "Hilux", 1000, 2000, 80, false, 7L, "7 BPM"
        );

        when(viaturaService.listarViaturasDTO(7L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/viaturas")
                        .with(authentication(auth(Role.MOTORISTA, 7L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].prefixo").value("PM-01"))
                .andExpect(jsonPath("$[0].combustivelAtualPercentual").value(80))
                .andExpect(jsonPath("$[0].batalhaoId").value(7));

        verify(viaturaService).listarViaturasDTO(7L);
    }

    @Test
    void createViaturaShouldInferBatalhaoFromAuthenticatedUser() throws Exception {
        Viatura persisted = new Viatura();
        Batalhao batalhao = new Batalhao();
        batalhao.setId(7L);
        batalhao.setNome("7 BPM");
        persisted.setId(11L);
        persisted.setBatalhao(batalhao);
        persisted.setPrefixo("PM-11");
        persisted.setPlaca("ABC1D23");
        persisted.setModelo("Hilux");
        persisted.setKmAtual(1000);
        persisted.setKmRevisao(2000);
        persisted.setCombustivelAtualPercentual(80);
        persisted.setManutencao(false);

        ViaturaByIdDTO response = new ViaturaByIdDTO(
                11L, "PM-11", "ABC1D23", "Hilux", 1000, 2000, 80, false, 7L, "7 BPM"
        );

        when(viaturaService.save(any(ViaturaDTO.class), any(User.class))).thenReturn(persisted);
        when(viaturaService.toByIdDTO(any(Viatura.class))).thenReturn(response);

        mockMvc.perform(post("/viatura")
                        .with(authentication(auth(Role.CHEFE_TRANSPORTE, 7L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "placa": "ABC1D23",
                                  "modelo": "Hilux",
                                  "manutencao": false,
                                  "prefixo": "PM-11",
                                  "kmAtual": 1000,
                                  "kmRevisao": 2000,
                                  "combustivelAtualPercentual": 80
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.batalhaoId").value(7));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(viaturaService).save(any(ViaturaDTO.class), userCaptor.capture());
        User serviceUser = userCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(7L, serviceUser.getBatalhao().getId());
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
