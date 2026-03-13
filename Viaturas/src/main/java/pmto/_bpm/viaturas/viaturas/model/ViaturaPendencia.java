package pmto._bpm.viaturas.viaturas.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pmto._bpm.viaturas.checklists.model.CheckList;
import pmto._bpm.viaturas.checklists.model.Problema;
import pmto._bpm.viaturas.users.model.User;
import pmto._bpm.viaturas.viaturas.dto.StatusPendenciaViatura;

import java.time.Instant;

@Entity
@Table(name = "viatura_pendencia")
@Getter @Setter
public class ViaturaPendencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "viatura_id")
    private Viatura viatura;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problema_id")
    private Problema problema;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPendenciaViatura status = StatusPendenciaViatura.EM_ANALISE;

    @Column(nullable = false)
    private Integer qtdRelatos = 1;

    private Instant primeiraOcorrenciaEm;
    private Instant ultimaOcorrenciaEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primeiro_checklist_id")
    private CheckList primeiroChecklist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ultimo_checklist_id")
    private CheckList ultimoChecklist;

    @Column(columnDefinition = "TEXT")
    private String ultimaObservacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolvido_por_id")
    private User resolvidoPor;

    private Instant resolvidoEm;

    @Column(columnDefinition = "TEXT")
    private String observacaoResolucao;

    public boolean aberta() {
        return status == StatusPendenciaViatura.EM_ANALISE
                || status == StatusPendenciaViatura.PENDENTE;
    }


}