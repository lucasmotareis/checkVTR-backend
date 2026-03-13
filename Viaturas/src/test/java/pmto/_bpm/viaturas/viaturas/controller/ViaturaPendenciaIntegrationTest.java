package pmto._bpm.viaturas.viaturas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import pmto._bpm.viaturas.auth.model.Role;
import pmto._bpm.viaturas.batalhao.model.Batalhao;
import pmto._bpm.viaturas.batalhao.repository.BatalhaoRepository;
import pmto._bpm.viaturas.checklists.dto.CategoriaProblema;
import pmto._bpm.viaturas.checklists.model.Problema;
import pmto._bpm.viaturas.checklists.repository.ProblemaRepository;
import pmto._bpm.viaturas.users.model.User;
import pmto._bpm.viaturas.users.repository.UserRepository;
import pmto._bpm.viaturas.viaturas.dto.StatusPendenciaViatura;
import pmto._bpm.viaturas.viaturas.model.Viatura;
import pmto._bpm.viaturas.viaturas.model.ViaturaPendencia;
import pmto._bpm.viaturas.viaturas.repository.ViaturaPendenciaRepository;
import pmto._bpm.viaturas.viaturas.repository.ViaturaRepository;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:viatura-pendencia-it;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.show-sql=false",
        "jwt.secret=01234567890123456789012345678901"
})
class ViaturaPendenciaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ViaturaPendenciaRepository viaturaPendenciaRepository;

    @Autowired
    private ViaturaRepository viaturaRepository;

    @Autowired
    private ProblemaRepository problemaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BatalhaoRepository batalhaoRepository;

    private final AtomicInteger sequence = new AtomicInteger(100000);

    @BeforeEach
    void cleanDatabase() {
        viaturaPendenciaRepository.deleteAll();
        problemaRepository.deleteAll();
        viaturaRepository.deleteAll();
        userRepository.deleteAll();
        batalhaoRepository.deleteAll();
    }

    @Test
    void resolverShouldAllowChiefFromSameBattalion() throws Exception {
        Fixture fixture = persistFixture();
        String body = objectMapper.writeValueAsString(Map.of("observacao", "resolvido na oficina"));

        mockMvc.perform(patch("/viaturas/pendencias/{pendenciaId}/resolver", fixture.pendencia().getId())
                        .with(authentication(auth(fixture.chefeMesmoBatalhao())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVIDO"))
                .andExpect(jsonPath("$.observacaoResolucao").value("resolvido na oficina"));

        ViaturaPendencia atualizada = viaturaPendenciaRepository.findById(fixture.pendencia().getId()).orElseThrow();
        assertEquals(StatusPendenciaViatura.RESOLVIDO, atualizada.getStatus());
        assertEquals(fixture.chefeMesmoBatalhao().getId(), atualizada.getResolvidoPor().getId());
        assertNotNull(atualizada.getResolvidoEm());
    }

    @Test
    void descartarShouldAllowChiefFromSameBattalion() throws Exception {
        Fixture fixture = persistFixture();
        String body = objectMapper.writeValueAsString(Map.of("observacao", "descartada apos revisao"));

        mockMvc.perform(patch("/viaturas/pendencias/{pendenciaId}/descartar", fixture.pendencia().getId())
                        .with(authentication(auth(fixture.chefeMesmoBatalhao())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DESCARTADO"))
                .andExpect(jsonPath("$.observacaoResolucao").value("descartada apos revisao"));

        ViaturaPendencia atualizada = viaturaPendenciaRepository.findById(fixture.pendencia().getId()).orElseThrow();
        assertEquals(StatusPendenciaViatura.DESCARTADO, atualizada.getStatus());
        assertEquals(fixture.chefeMesmoBatalhao().getId(), atualizada.getResolvidoPor().getId());
        assertNotNull(atualizada.getResolvidoEm());
    }

    @Test
    void resolverShouldReturnForbiddenForChiefFromAnotherBattalion() throws Exception {
        Fixture fixture = persistFixture();
        String body = objectMapper.writeValueAsString(Map.of("observacao", "tentativa fora de escopo"));

        mockMvc.perform(patch("/viaturas/pendencias/{pendenciaId}/resolver", fixture.pendencia().getId())
                        .with(authentication(auth(fixture.chefeOutroBatalhao())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        ViaturaPendencia atualizada = viaturaPendenciaRepository.findById(fixture.pendencia().getId()).orElseThrow();
        assertEquals(StatusPendenciaViatura.PENDENTE, atualizada.getStatus());
        assertNull(atualizada.getResolvidoPor());
        assertNull(atualizada.getResolvidoEm());
    }

    @Test
    void resolverShouldReturnForbiddenForMotorista() throws Exception {
        Fixture fixture = persistFixture();
        String body = objectMapper.writeValueAsString(Map.of("observacao", "motorista sem permissao"));

        mockMvc.perform(patch("/viaturas/pendencias/{pendenciaId}/resolver", fixture.pendencia().getId())
                        .with(authentication(auth(fixture.motoristaMesmoBatalhao())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        ViaturaPendencia atualizada = viaturaPendenciaRepository.findById(fixture.pendencia().getId()).orElseThrow();
        assertEquals(StatusPendenciaViatura.PENDENTE, atualizada.getStatus());
        assertNull(atualizada.getResolvidoPor());
        assertNull(atualizada.getResolvidoEm());
    }

    @Test
    void descartarShouldReturnForbiddenForChiefFromAnotherBattalion() throws Exception {
        Fixture fixture = persistFixture();
        String body = objectMapper.writeValueAsString(Map.of("observacao", "tentativa fora de escopo"));

        mockMvc.perform(patch("/viaturas/pendencias/{pendenciaId}/descartar", fixture.pendencia().getId())
                        .with(authentication(auth(fixture.chefeOutroBatalhao())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        ViaturaPendencia atualizada = viaturaPendenciaRepository.findById(fixture.pendencia().getId()).orElseThrow();
        assertEquals(StatusPendenciaViatura.PENDENTE, atualizada.getStatus());
        assertNull(atualizada.getResolvidoPor());
        assertNull(atualizada.getResolvidoEm());
    }

    @Test
    void descartarShouldReturnBadRequestWhenPendenciaAlreadyFinalized() throws Exception {
        Fixture fixture = persistFixture();
        String resolverBody = objectMapper.writeValueAsString(Map.of("observacao", "resolvida"));
        String descartarBody = objectMapper.writeValueAsString(Map.of("observacao", "nao deveria descartar"));

        mockMvc.perform(patch("/viaturas/pendencias/{pendenciaId}/resolver", fixture.pendencia().getId())
                        .with(authentication(auth(fixture.chefeMesmoBatalhao())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resolverBody))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/viaturas/pendencias/{pendenciaId}/descartar", fixture.pendencia().getId())
                        .with(authentication(auth(fixture.chefeMesmoBatalhao())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(descartarBody))
                .andExpect(status().isBadRequest());
    }

    private Fixture persistFixture() {
        Batalhao batalhaoA = persistBatalhao("8 BPM");
        Batalhao batalhaoB = persistBatalhao("9 BPM");

        User chefeMesmoBatalhao = persistUser(batalhaoA, Role.CHEFE_TRANSPORTE, "Chefe A");
        User chefeOutroBatalhao = persistUser(batalhaoB, Role.CHEFE_TRANSPORTE, "Chefe B");
        User motoristaMesmoBatalhao = persistUser(batalhaoA, Role.MOTORISTA, "Motorista A");

        Viatura viatura = persistViatura(batalhaoA, "PM-" + sequence.incrementAndGet());
        Problema problema = persistProblema(CategoriaProblema.LUZES, "Farol queimado");

        ViaturaPendencia pendencia = new ViaturaPendencia();
        pendencia.setViatura(viatura);
        pendencia.setProblema(problema);
        pendencia.setStatus(StatusPendenciaViatura.PENDENTE);
        pendencia.setQtdRelatos(2);
        pendencia.setPrimeiraOcorrenciaEm(Instant.parse("2026-03-10T10:00:00Z"));
        pendencia.setUltimaOcorrenciaEm(Instant.parse("2026-03-11T10:00:00Z"));
        pendencia.setUltimaObservacao("sem iluminacao");
        pendencia = viaturaPendenciaRepository.saveAndFlush(pendencia);

        return new Fixture(pendencia, chefeMesmoBatalhao, chefeOutroBatalhao, motoristaMesmoBatalhao);
    }

    private Batalhao persistBatalhao(String nome) {
        Batalhao batalhao = new Batalhao();
        batalhao.setNome(nome);
        return batalhaoRepository.saveAndFlush(batalhao);
    }

    private User persistUser(Batalhao batalhao, Role role, String nomeGuerra) {
        int next = sequence.incrementAndGet();

        User user = new User();
        user.setNomeGuerra(nomeGuerra);
        user.setGraduacao("SGT");
        user.setSenha("senha");
        user.setRole(role);
        user.setMatricula(String.format("%06d", next));
        user.setCPF(String.format("%011d", next));
        user.setBatalhao(batalhao);
        return userRepository.saveAndFlush(user);
    }

    private Viatura persistViatura(Batalhao batalhao, String prefixo) {
        Viatura viatura = new Viatura();
        viatura.setBatalhao(batalhao);
        viatura.setPrefixo(prefixo);
        viatura.setPlaca(prefixo + "-ABC");
        viatura.setModelo("Hilux");
        viatura.setKmAtual(12000);
        viatura.setKmRevisao(20000);
        viatura.setCombustivelAtualPercentual(75);
        viatura.setManutencao(false);
        return viaturaRepository.saveAndFlush(viatura);
    }

    private Problema persistProblema(CategoriaProblema categoria, String descricao) {
        Problema problema = new Problema();
        problema.setCategoria(categoria);
        problema.setDescricao(descricao);
        return problemaRepository.saveAndFlush(problema);
    }

    private UsernamePasswordAuthenticationToken auth(User user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private record Fixture(
            ViaturaPendencia pendencia,
            User chefeMesmoBatalhao,
            User chefeOutroBatalhao,
            User motoristaMesmoBatalhao
    ) {
    }
}
