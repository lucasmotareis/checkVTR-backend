package pmto._bpm.viaturas.viaturas.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import pmto._bpm.viaturas.viaturas.dto.ZonaViaturaCidadeQuadroDTO;
import pmto._bpm.viaturas.viaturas.dto.ZonaViaturaCidadeResponseDTO;
import pmto._bpm.viaturas.viaturas.dto.ZonaViaturaQuadroDTO;
import pmto._bpm.viaturas.viaturas.dto.ZonaViaturaResponseDTO;
import pmto._bpm.viaturas.viaturas.service.ZonaViaturaService;

@WebMvcTest(ZonaViaturaController.class)
@Import(TestSecurityConfig.class)
class ZonaViaturaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ZonaViaturaService zonaViaturaService;

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
    void getQuadroShouldReturnSemZonaAndConfiguredZones() throws Exception {
        when(zonaViaturaService.listarQuadro(any(User.class))).thenReturn(List.of(
                new ZonaViaturaQuadroDTO(null, "Sem zona", "Sem zona", true, List.of(), List.of()),
                new ZonaViaturaQuadroDTO(
                        1L,
                        "1a CIA",
                        "1a CIA",
                        false,
                        List.of(new ZonaViaturaCidadeQuadroDTO(7L, "Paraiso", "Paraiso", false, List.of())),
                        List.of()
                )
        ));

        mockMvc.perform(get("/viaturas-zonas/quadro")
                        .with(authentication(auth(Role.CHEFE_TRANSPORTE, 7L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].label").value("Sem zona"))
                .andExpect(jsonPath("$[1].cidades[0].cidade").value("Paraiso"));
    }

    @Test
    void createZonaShouldReturnCreatedZone() throws Exception {
        when(zonaViaturaService.criar(any(), any(User.class)))
                .thenReturn(new ZonaViaturaResponseDTO(4L, "2a CIA", 2, "2a CIA"));

        mockMvc.perform(post("/viaturas-zonas")
                        .with(authentication(auth(Role.CHEFE_TRANSPORTE, 7L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "2a CIA"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.label").value("2a CIA"));
    }

    @Test
    void reorderZonaShouldReturnNoContent() throws Exception {
        doNothing().when(zonaViaturaService).reordenar(any(), any(User.class));

        mockMvc.perform(patch("/viaturas-zonas/ordem")
                        .with(authentication(auth(Role.CHEFE_TRANSPORTE, 7L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "zonaIds": [2, 1]
                                }
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void createCidadeShouldReturnCreatedCity() throws Exception {
        when(zonaViaturaService.criarCidade(any(), any(), any(User.class)))
                .thenReturn(new ZonaViaturaCidadeResponseDTO(9L, 4L, "Divinopolis", 2, "Divinopolis"));

        mockMvc.perform(post("/viaturas-zonas/4/cidades")
                        .with(authentication(auth(Role.CHEFE_TRANSPORTE, 7L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cidade": "Divinopolis"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.zonaId").value(4))
                .andExpect(jsonPath("$.cidade").value("Divinopolis"));
    }

    @Test
    void updateCidadeShouldReturnUpdatedCity() throws Exception {
        when(zonaViaturaService.atualizarCidade(any(), any(), any(User.class)))
                .thenReturn(new ZonaViaturaCidadeResponseDTO(9L, 4L, "Araguacema", 2, "Araguacema"));

        mockMvc.perform(put("/viaturas-zonas/cidades/9")
                        .with(authentication(auth(Role.CHEFE_TRANSPORTE, 7L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cidade": "Araguacema"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cidade").value("Araguacema"));
    }

    @Test
    void deleteCidadeShouldReturnNoContent() throws Exception {
        doNothing().when(zonaViaturaService).deletarCidade(any(), any(User.class));

        mockMvc.perform(delete("/viaturas-zonas/cidades/9")
                        .with(authentication(auth(Role.CHEFE_TRANSPORTE, 7L))))
                .andExpect(status().isNoContent());
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
