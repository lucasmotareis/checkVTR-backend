package pmto._bpm.viaturas.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import pmto._bpm.viaturas.analytics.dto.TopItemDTO;
import pmto._bpm.viaturas.analytics.repository.AnalyticsRepository;
import pmto._bpm.viaturas.auth.model.Role;
import pmto._bpm.viaturas.batalhao.model.Batalhao;
import pmto._bpm.viaturas.checklists.model.CheckList;
import pmto._bpm.viaturas.checklists.model.CheckListProblema;
import pmto._bpm.viaturas.checklists.model.Problema;
import pmto._bpm.viaturas.notifications.dto.NotificacaoStatsDTO;
import pmto._bpm.viaturas.notifications.model.Notification;
import pmto._bpm.viaturas.notifications.repository.NotificationRepository;
import pmto._bpm.viaturas.users.model.User;
import pmto._bpm.viaturas.viaturas.dto.ViaturaComBadgeDTO;
import pmto._bpm.viaturas.viaturas.model.Viatura;
import pmto._bpm.viaturas.viaturas.repository.ViaturaRepository;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class RepositoryQueryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ViaturaRepository viaturaRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AnalyticsRepository analyticsRepository;

    @Test
    void findViaturasComBadgeShouldCountOnlyUnseenChecklists() {
        Batalhao batalhao = persistBatalhao("8 BPM");
        User user = persistUser(batalhao, "111111", "12345678901");
        Viatura viatura = persistViatura(batalhao, "PM-01");

        persistChecklist(viatura, user, false);
        persistChecklist(viatura, user, true);
        entityManager.flush();

        List<ViaturaComBadgeDTO> result = viaturaRepository.findViaturasComBadge(batalhao.getId());

        assertEquals(1, result.size());
        assertEquals(viatura.getId(), result.get(0).id());
        assertEquals(1L, result.get(0).checklistsNaoVistos());
    }

    @Test
    void getStatsShouldReturnAggregatedNotificationCounts() {
        Batalhao batalhao = persistBatalhao("6 BPM");

        Notification n1 = new Notification();
        n1.setBatalhao(batalhao);
        n1.setTitulo("T1");
        n1.setMensagem("M1");
        notificationRepository.save(n1);

        Notification n2 = new Notification();
        n2.setBatalhao(batalhao);
        n2.setTitulo("T2");
        n2.setMensagem("M2");
        notificationRepository.save(n2);
        entityManager.flush();

        NotificacaoStatsDTO stats = notificationRepository.getStats(
                batalhao.getId(),
                Instant.EPOCH,
                Instant.EPOCH,
                Instant.EPOCH
        );

        assertEquals(2L, stats.total());
        assertEquals(2L, stats.today());
        assertEquals(2L, stats.thisWeek());
        assertEquals(2L, stats.thisMonth());
        assertNotNull(stats.lastCreatedAt());
    }

    @Test
    void topProblemasShouldReturnRankedCounts() {
        Batalhao batalhao = persistBatalhao("5 BPM");
        User user = persistUser(batalhao, "222222", "12345678902");
        Viatura viatura = persistViatura(batalhao, "PM-02");

        Problema pneu = persistProblema("Mecanico", "Pneu");
        Problema freio = persistProblema("Mecanico", "Freio");

        CheckList c1 = persistChecklist(viatura, user, false);
        CheckList c2 = persistChecklist(viatura, user, false);

        persistChecklistProblema(c1, pneu, "p1");
        persistChecklistProblema(c2, pneu, "p2");
        persistChecklistProblema(c2, freio, "f1");
        entityManager.flush();

        List<TopItemDTO> top = analyticsRepository.topProblemas(
                batalhao.getId(),
                Instant.EPOCH,
                Instant.now().plusSeconds(60),
                PageRequest.of(0, 10)
        );

        assertFalse(top.isEmpty());
        assertEquals("Pneu", top.get(0).descricao());
        assertEquals(2L, top.get(0).total());
    }

    private Batalhao persistBatalhao(String nome) {
        Batalhao batalhao = new Batalhao();
        batalhao.setNome(nome);
        return entityManager.persistAndFlush(batalhao);
    }

    private User persistUser(Batalhao batalhao, String matricula, String cpf) {
        User user = new User();
        user.setNomeGuerra("Silva");
        user.setGraduacao("SGT");
        user.setSenha("senha");
        user.setRole(Role.MOTORISTA);
        user.setMatricula(matricula);
        user.setCPF(cpf);
        user.setBatalhao(batalhao);
        return entityManager.persistAndFlush(user);
    }

    private Viatura persistViatura(Batalhao batalhao, String prefixo) {
        Viatura v = new Viatura();
        v.setBatalhao(batalhao);
        v.setPrefixo(prefixo);
        v.setPlaca(prefixo + "-ABC");
        v.setModelo("Hilux");
        v.setKmAtual(1000);
        v.setKmRevisao(2000);
        v.setManutencao(false);
        return entityManager.persistAndFlush(v);
    }

    private Problema persistProblema(String categoria, String descricao) {
        Problema p = new Problema();
        p.setCategoria(categoria);
        p.setDescricao(descricao);
        return entityManager.persistAndFlush(p);
    }

    private CheckList persistChecklist(Viatura viatura, User user, boolean visto) {
        CheckList checkList = new CheckList();
        checkList.setViatura(viatura);
        checkList.setUsuario(user);
        checkList.setVistoPeloChefe(visto);
        checkList.setKmAtual(1200);
        checkList.setKmRevisao(2200);
        return entityManager.persistAndFlush(checkList);
    }

    private void persistChecklistProblema(CheckList checkList, Problema problema, String observacao) {
        CheckListProblema cp = new CheckListProblema();
        cp.setChecklist(checkList);
        cp.setProblema(problema);
        cp.setObservacao(observacao);
        entityManager.persist(cp);
    }
}
