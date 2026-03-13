package pmto._bpm.viaturas.viaturas.controller;

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
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import pmto._bpm.viaturas.auth.model.Role;
import pmto._bpm.viaturas.auth.security.JwtAuthFilter;
import pmto._bpm.viaturas.batalhao.model.Batalhao;
import pmto._bpm.viaturas.testsupport.TestSecurityConfig;
import pmto._bpm.viaturas.users.model.User;
import pmto._bpm.viaturas.viaturas.dto.StatusPendenciaViatura;
import pmto._bpm.viaturas.viaturas.model.Viatura;
import pmto._bpm.viaturas.viaturas.model.ViaturaPendencia;
import pmto._bpm.viaturas.viaturas.service.ViaturaPendenciaService;
import pmto._bpm.viaturas.checklists.model.Problema;
import pmto._bpm.viaturas.checklists.dto.CategoriaProblema;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ViaturaPendenciaController.class)
@Import(TestSecurityConfig.class)
class ViaturaPendenciaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ViaturaPendenciaService viaturaPendenciaService;

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
    void listarAbertasShouldReturnForbiddenWhenServiceDeniesScope() throws Exception {
        when(viaturaPendenciaService.listarAbertasPorViatura(eq(10L), any(User.class)))
                .thenThrow(new AccessDeniedException("fora de escopo"));

        mockMvc.perform(get("/viaturas/pendencias/viatura/10")
                        .with(authentication(auth(Role.MOTORISTA, 1L))))
                .andExpect(status().isForbidden())
                .andExpect(content().string("fora de escopo"));
    }

    @Test
    void resolverShouldReturnBadRequestWhenServiceRejectsStateTransition() throws Exception {
        when(viaturaPendenciaService.resolverPendencia(eq(99L), eq("feito"), any(User.class)))
                .thenThrow(new IllegalArgumentException("Pendencia ja finalizada."));

        String body = objectMapper.writeValueAsString(java.util.Map.of("observacao", "feito"));

        mockMvc.perform(patch("/viaturas/pendencias/99/resolver")
                        .with(authentication(auth(Role.CHEFE_TRANSPORTE, 1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Pendencia ja finalizada."));
    }

    @Test
    void resolverShouldReturnNotFoundWhenPendenciaDoesNotExist() throws Exception {
        when(viaturaPendenciaService.resolverPendencia(eq(404L), eq("x"), any(User.class)))
                .thenThrow(new NoSuchElementException("Pendencia nao encontrada."));

        String body = objectMapper.writeValueAsString(java.util.Map.of("observacao", "x"));

        mockMvc.perform(patch("/viaturas/pendencias/404/resolver")
                        .with(authentication(auth(Role.CHEFE_TRANSPORTE, 1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Pendencia nao encontrada."));
    }

    @Test
    void listarAbertasShouldSerializeResponsePayload() throws Exception {
        ViaturaPendencia pendencia = pendenciaFixture(StatusPendenciaViatura.PENDENTE);
        when(viaturaPendenciaService.listarAbertasPorViatura(eq(10L), any(User.class)))
                .thenReturn(List.of(pendencia));

        mockMvc.perform(get("/viaturas/pendencias/viatura/10")
                        .with(authentication(auth(Role.CHEFE_TRANSPORTE, 1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].viaturaId").value(10))
                .andExpect(jsonPath("$[0].problemaId").value(50))
                .andExpect(jsonPath("$[0].status").value("PENDENTE"));
    }

    private ViaturaPendencia pendenciaFixture(StatusPendenciaViatura status) {
        Viatura viatura = new Viatura();
        viatura.setId(10L);
        viatura.setPrefixo("PM-10");

        Problema problema = new Problema();
        problema.setId(50L);
        problema.setCategoria(CategoriaProblema.LUZES);
        problema.setDescricao("Farol queimado");

        User resolvedBy = new User();
        resolvedBy.setNomeGuerra("Chefe");

        ViaturaPendencia pendencia = new ViaturaPendencia();
        pendencia.setId(1L);
        pendencia.setViatura(viatura);
        pendencia.setProblema(problema);
        pendencia.setStatus(status);
        pendencia.setQtdRelatos(2);
        pendencia.setPrimeiraOcorrenciaEm(Instant.parse("2026-03-10T10:00:00Z"));
        pendencia.setUltimaOcorrenciaEm(Instant.parse("2026-03-11T10:00:00Z"));
        pendencia.setUltimaObservacao("observacao");
        pendencia.setResolvidoPor(resolvedBy);
        pendencia.setResolvidoEm(Instant.parse("2026-03-12T10:00:00Z"));
        pendencia.setObservacaoResolucao("ok");
        return pendencia;
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
