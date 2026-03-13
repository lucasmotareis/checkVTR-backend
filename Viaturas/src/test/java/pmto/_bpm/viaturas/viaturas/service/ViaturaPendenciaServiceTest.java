package pmto._bpm.viaturas.viaturas.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.TestPropertySource;
import pmto._bpm.viaturas.auth.model.Role;
import pmto._bpm.viaturas.batalhao.model.Batalhao;
import pmto._bpm.viaturas.checklists.dto.CategoriaProblema;
import pmto._bpm.viaturas.checklists.model.CheckList;
import pmto._bpm.viaturas.checklists.model.CheckListProblema;
import pmto._bpm.viaturas.checklists.model.Problema;
import pmto._bpm.viaturas.users.model.User;
import pmto._bpm.viaturas.viaturas.dto.StatusPendenciaViatura;
import pmto._bpm.viaturas.viaturas.model.Viatura;
import pmto._bpm.viaturas.viaturas.model.ViaturaPendencia;
import pmto._bpm.viaturas.viaturas.repository.ViaturaPendenciaRepository;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Import(ViaturaPendenciaService.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect")
class ViaturaPendenciaServiceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ViaturaPendenciaService viaturaPendenciaService;

    @Autowired
    private ViaturaPendenciaRepository viaturaPendenciaRepository;

    @Test
    void registrarPendenciasCriticasShouldCreateOpenPendenciaOnFirstReport() {
        Batalhao batalhao = persistBatalhao("8 BPM");
        User motorista = persistUser(batalhao, Role.MOTORISTA, "111111", "11111111111");
        Viatura viatura = persistViatura(batalhao, "PM-101");
        Problema problema = persistProblema(CategoriaProblema.LUZES, "Farol dianteiro queimado");

        CheckList checklist = persistChecklistComProblema(
                viatura, motorista, problema, "lado esquerdo", Instant.parse("2026-03-10T10:00:00Z")
        );

        viaturaPendenciaService.registrarPendenciasCriticas(checklist);

        List<ViaturaPendencia> pendencias = viaturaPendenciaRepository.findAll();
        assertEquals(1, pendencias.size());

        ViaturaPendencia pendencia = pendencias.get(0);
        assertEquals(StatusPendenciaViatura.EM_ANALISE, pendencia.getStatus());
        assertEquals(1, pendencia.getQtdRelatos());
        assertEquals("lado esquerdo", pendencia.getUltimaObservacao());
        assertEquals(checklist.getId(), pendencia.getPrimeiroChecklist().getId());
        assertEquals(checklist.getId(), pendencia.getUltimoChecklist().getId());
    }

    @Test
    void registrarPendenciasCriticasShouldPromoteToPendenteOnSecondReport() {
        Batalhao batalhao = persistBatalhao("7 BPM");
        User motorista = persistUser(batalhao, Role.MOTORISTA, "222222", "22222222222");
        Viatura viatura = persistViatura(batalhao, "PM-202");
        Problema problema = persistProblema(CategoriaProblema.PNEUS, "Pneu careca");

        CheckList checklist1 = persistChecklistComProblema(
                viatura, motorista, problema, "frontal", Instant.parse("2026-03-10T09:00:00Z")
        );
        CheckList checklist2 = persistChecklistComProblema(
                viatura, motorista, problema, "traseiro", Instant.parse("2026-03-11T09:00:00Z")
        );

        viaturaPendenciaService.registrarPendenciasCriticas(checklist1);
        viaturaPendenciaService.registrarPendenciasCriticas(checklist2);

        List<ViaturaPendencia> pendencias = viaturaPendenciaRepository.findAll();
        assertEquals(1, pendencias.size());

        ViaturaPendencia pendencia = pendencias.get(0);
        assertEquals(StatusPendenciaViatura.PENDENTE, pendencia.getStatus());
        assertEquals(2, pendencia.getQtdRelatos());
        assertEquals("traseiro", pendencia.getUltimaObservacao());
        assertEquals(checklist1.getId(), pendencia.getPrimeiroChecklist().getId());
        assertEquals(checklist2.getId(), pendencia.getUltimoChecklist().getId());
    }

    @Test
    void registrarPendenciasCriticasShouldIgnoreNonCriticalCategory() {
        Batalhao batalhao = persistBatalhao("6 BPM");
        User motorista = persistUser(batalhao, Role.MOTORISTA, "333333", "33333333333");
        Viatura viatura = persistViatura(batalhao, "PM-303");
        Problema problema = persistProblema(CategoriaProblema.MOTOR, "Vazamento de oleo");

        CheckList checklist = persistChecklistComProblema(
                viatura, motorista, problema, "leve", Instant.parse("2026-03-10T08:00:00Z")
        );

        viaturaPendenciaService.registrarPendenciasCriticas(checklist);

        assertEquals(0, viaturaPendenciaRepository.count());
    }

    @Test
    void resolverPendenciaShouldDenyMotorista() {
        Batalhao batalhao = persistBatalhao("5 BPM");
        User motorista = persistUser(batalhao, Role.MOTORISTA, "444444", "44444444444");
        ViaturaPendencia pendencia = persistPendenciaAberta(batalhao, "PM-404");

        assertThrows(
                AccessDeniedException.class,
                () -> viaturaPendenciaService.resolverPendencia(pendencia.getId(), "ok", motorista)
        );
    }

    @Test
    void resolverPendenciaShouldDenyChiefFromAnotherBattalion() {
        Batalhao batalhaoA = persistBatalhao("4 BPM");
        Batalhao batalhaoB = persistBatalhao("3 BPM");
        User chefeOutroBatalhao = persistUser(batalhaoB, Role.CHEFE_TRANSPORTE, "555555", "55555555555");
        ViaturaPendencia pendencia = persistPendenciaAberta(batalhaoA, "PM-505");

        assertThrows(
                AccessDeniedException.class,
                () -> viaturaPendenciaService.resolverPendencia(pendencia.getId(), "ok", chefeOutroBatalhao)
        );
    }

    @Test
    void resolverPendenciaShouldResolveWhenChiefIsFromSameBattalion() {
        Batalhao batalhao = persistBatalhao("2 BPM");
        User chefe = persistUser(batalhao, Role.CHEFE_TRANSPORTE, "666666", "66666666666");
        ViaturaPendencia pendencia = persistPendenciaAberta(batalhao, "PM-606");

        ViaturaPendencia resolvida = viaturaPendenciaService.resolverPendencia(
                pendencia.getId(), "resolvido na oficina", chefe
        );

        assertEquals(StatusPendenciaViatura.RESOLVIDO, resolvida.getStatus());
        assertEquals(chefe.getId(), resolvida.getResolvidoPor().getId());
        assertEquals("resolvido na oficina", resolvida.getObservacaoResolucao());
        assertNotNull(resolvida.getResolvidoEm());
    }

    @Test
    void descartarPendenciaShouldFailWhenAlreadyFinalized() {
        Batalhao batalhao = persistBatalhao("1 BPM");
        User chefe = persistUser(batalhao, Role.CHEFE_TRANSPORTE, "777777", "77777777777");
        ViaturaPendencia pendencia = persistPendenciaAberta(batalhao, "PM-707");

        viaturaPendenciaService.resolverPendencia(pendencia.getId(), "feito", chefe);

        assertThrows(
                IllegalArgumentException.class,
                () -> viaturaPendenciaService.descartarPendencia(pendencia.getId(), "nao se aplica", chefe)
        );
    }

    @Test
    void listarAbertasPorViaturaShouldEnforceBattalionScope() {
        Batalhao batalhaoA = persistBatalhao("9 BPM");
        Batalhao batalhaoB = persistBatalhao("10 BPM");
        User userOutroBatalhao = persistUser(batalhaoB, Role.MOTORISTA, "888888", "88888888888");
        User userMesmoBatalhao = persistUser(batalhaoA, Role.MOTORISTA, "999999", "99999999999");

        ViaturaPendencia pendencia = persistPendenciaAberta(batalhaoA, "PM-808");
        Long viaturaId = pendencia.getViatura().getId();

        assertThrows(
                AccessDeniedException.class,
                () -> viaturaPendenciaService.listarAbertasPorViatura(viaturaId, userOutroBatalhao)
        );

        List<ViaturaPendencia> abertas = viaturaPendenciaService.listarAbertasPorViatura(viaturaId, userMesmoBatalhao);
        assertEquals(1, abertas.size());
        assertEquals(pendencia.getId(), abertas.get(0).getId());
    }

    private ViaturaPendencia persistPendenciaAberta(Batalhao batalhao, String prefixo) {
        User motorista = persistUser(batalhao, Role.MOTORISTA, prefixo + "M", cpfFrom(prefixo, "01"));
        Viatura viatura = persistViatura(batalhao, prefixo);
        Problema problema = persistProblema(CategoriaProblema.ITENS_OBRIGATORIOS, "Extintor vencido");
        CheckList checklist = persistChecklistComProblema(
                viatura, motorista, problema, "validade vencida", Instant.parse("2026-03-10T07:00:00Z")
        );
        viaturaPendenciaService.registrarPendenciasCriticas(checklist);
        return viaturaPendenciaRepository.findAll().get(viaturaPendenciaRepository.findAll().size() - 1);
    }

    private Batalhao persistBatalhao(String nome) {
        Batalhao batalhao = new Batalhao();
        batalhao.setNome(nome);
        return entityManager.persistAndFlush(batalhao);
    }

    private User persistUser(Batalhao batalhao, Role role, String matricula, String cpf) {
        User user = new User();
        user.setNomeGuerra("Silva " + matricula);
        user.setGraduacao("SGT");
        user.setSenha("senha");
        user.setRole(role);
        user.setMatricula(matricula);
        user.setCPF(cpf);
        user.setBatalhao(batalhao);
        return entityManager.persistAndFlush(user);
    }

    private Viatura persistViatura(Batalhao batalhao, String prefixo) {
        Viatura viatura = new Viatura();
        viatura.setBatalhao(batalhao);
        viatura.setPrefixo(prefixo);
        viatura.setPlaca(prefixo + "-ABC");
        viatura.setModelo("Hilux");
        viatura.setKmAtual(1000);
        viatura.setKmRevisao(2000);
        viatura.setManutencao(false);
        return entityManager.persistAndFlush(viatura);
    }

    private Problema persistProblema(CategoriaProblema categoria, String descricao) {
        Problema problema = new Problema();
        problema.setCategoria(categoria);
        problema.setDescricao(descricao);
        return entityManager.persistAndFlush(problema);
    }

    private CheckList persistChecklistComProblema(
            Viatura viatura,
            User usuario,
            Problema problema,
            String observacao,
            Instant data
    ) {
        CheckList checklist = new CheckList();
        checklist.setViatura(viatura);
        checklist.setUsuario(usuario);
        checklist.setKmAtual(1200);
        checklist.setKmRevisao(2200);
        checklist.setData(data);
        checklist = entityManager.persistAndFlush(checklist);

        CheckListProblema cp = new CheckListProblema();
        cp.setChecklist(checklist);
        cp.setProblema(problema);
        cp.setObservacao(observacao);
        checklist.getProblemas().add(cp);
        entityManager.persistAndFlush(cp);
        return checklist;
    }

    private String cpfFrom(String seed, String suffix) {
        String digits = seed.replaceAll("\\D", "");
        String base = (digits + "00000000000").substring(0, 9);
        return base + suffix;
    }
}
