package pmto._bpm.viaturas.viaturas.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pmto._bpm.viaturas.auth.model.Role;
import pmto._bpm.viaturas.checklists.model.CheckList;
import pmto._bpm.viaturas.checklists.model.CheckListProblema;
import pmto._bpm.viaturas.checklists.model.Problema;
import pmto._bpm.viaturas.users.model.User;
import pmto._bpm.viaturas.viaturas.dto.StatusPendenciaViatura;
import pmto._bpm.viaturas.viaturas.model.Viatura;
import pmto._bpm.viaturas.viaturas.model.ViaturaPendencia;
import pmto._bpm.viaturas.viaturas.repository.ViaturaPendenciaRepository;
import pmto._bpm.viaturas.viaturas.repository.ViaturaRepository;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
public class ViaturaPendenciaService {

    private final ViaturaPendenciaRepository viaturaPendenciaRepository;
    private final ViaturaRepository viaturaRepository;

    public ViaturaPendenciaService(ViaturaPendenciaRepository viaturaPendenciaRepository,
                                   ViaturaRepository viaturaRepository) {
        this.viaturaPendenciaRepository = viaturaPendenciaRepository;
        this.viaturaRepository = viaturaRepository;
    }

    @Transactional
    public void registrarPendenciasCriticas(CheckList checkList) {
        if (checkList.getProblemas() == null || checkList.getProblemas().isEmpty()) {
            return;
        }

        Long viaturaId = checkList.getViatura() != null ? checkList.getViatura().getId() : null;
        if (viaturaId == null) {
            throw new IllegalArgumentException("Checklist sem viatura.");
        }
        viaturaRepository.findByIdForUpdate(viaturaId)
                .orElseThrow(() -> new NoSuchElementException("Viatura nao encontrada."));

        Instant dataOcorrencia = checkList.getData() != null
                ? checkList.getData()
                : Instant.now();

        for (CheckListProblema item : checkList.getProblemas()) {
            Problema problema = item.getProblema();

            if (problema == null || problema.getCategoria() == null) {
                continue;
            }

            if (!problema.deveMonitorarNaViatura()) {
                continue;
            }

            ViaturaPendencia pendencia = viaturaPendenciaRepository
                    .findFirstByViaturaIdAndProblemaIdAndStatusIn(
                            checkList.getViatura().getId(),
                            problema.getId(),
                            List.of(
                                    StatusPendenciaViatura.EM_ANALISE,
                                    StatusPendenciaViatura.PENDENTE
                            )
                    )
                    .orElseGet(() -> criarNovaPendencia(checkList, item, dataOcorrencia));

            if (pendencia.getId() == null && pendencia.getQtdRelatos() == 1) {
                viaturaPendenciaRepository.save(pendencia);
                continue;
            }

            pendencia.setQtdRelatos(pendencia.getQtdRelatos() + 1);
            pendencia.setUltimaOcorrenciaEm(dataOcorrencia);
            pendencia.setUltimoChecklist(checkList);

            if (item.getObservacao() != null && !item.getObservacao().isBlank()) {
                pendencia.setUltimaObservacao(item.getObservacao());
            }

            if (pendencia.getQtdRelatos() >= 2) {
                pendencia.setStatus(StatusPendenciaViatura.PENDENTE);
            } else {
                pendencia.setStatus(StatusPendenciaViatura.EM_ANALISE);
            }

            viaturaPendenciaRepository.save(pendencia);
        }
    }

    private ViaturaPendencia criarNovaPendencia(CheckList checkList,
                                                CheckListProblema item,
                                                Instant dataOcorrencia) {
        ViaturaPendencia nova = new ViaturaPendencia();
        nova.setViatura(checkList.getViatura());
        nova.setProblema(item.getProblema());
        nova.setStatus(StatusPendenciaViatura.EM_ANALISE);
        nova.setQtdRelatos(1);
        nova.setPrimeiraOcorrenciaEm(dataOcorrencia);
        nova.setUltimaOcorrenciaEm(dataOcorrencia);
        nova.setPrimeiroChecklist(checkList);
        nova.setUltimoChecklist(checkList);
        nova.setUltimaObservacao(item.getObservacao());
        return nova;
    }

    @Transactional
    public ViaturaPendencia resolverPendencia(Long pendenciaId, String observacaoResolucao, User usuario) {
        validarPermissaoResolucao(usuario);
        ViaturaPendencia pendencia = buscarPendenciaNoEscopo(pendenciaId, usuario);

        if (!pendencia.aberta()) {
            throw new IllegalArgumentException("Pendencia ja finalizada.");
        }

        pendencia.setStatus(StatusPendenciaViatura.RESOLVIDO);
        pendencia.setResolvidoPor(usuario);
        pendencia.setResolvidoEm(Instant.now());
        pendencia.setObservacaoResolucao(observacaoResolucao);

        return viaturaPendenciaRepository.save(pendencia);
    }

    @Transactional
    public ViaturaPendencia descartarPendencia(Long pendenciaId, String observacao, User usuario) {
        validarPermissaoResolucao(usuario);
        ViaturaPendencia pendencia = buscarPendenciaNoEscopo(pendenciaId, usuario);

        if (!pendencia.aberta()) {
            throw new IllegalArgumentException("Pendencia ja finalizada.");
        }

        pendencia.setStatus(StatusPendenciaViatura.DESCARTADO);
        pendencia.setResolvidoPor(usuario);
        pendencia.setResolvidoEm(Instant.now());
        pendencia.setObservacaoResolucao(observacao);

        return viaturaPendenciaRepository.save(pendencia);
    }

    @Transactional(readOnly = true)
    public List<ViaturaPendencia> listarAbertasPorViatura(Long viaturaId, User usuario) {
        Viatura viatura = viaturaRepository.findById(viaturaId)
                .orElseThrow(() -> new NoSuchElementException("Viatura nao encontrada."));

        validarEscopoBatalhao(usuario, viatura.getBatalhao().getId());

        return viaturaPendenciaRepository.findByViaturaIdAndStatusInOrderByUltimaOcorrenciaEmDesc(
                viaturaId,
                List.of(
                        StatusPendenciaViatura.EM_ANALISE,
                        StatusPendenciaViatura.PENDENTE
                )
        );
    }

    private void validarPermissaoResolucao(User usuario) {
        if (usuario == null || usuario.getRole() != Role.CHEFE_TRANSPORTE) {
            throw new AccessDeniedException("Somente o chefe de transporte pode resolver pendencias.");
        }
    }

    private ViaturaPendencia buscarPendenciaNoEscopo(Long pendenciaId, User usuario) {
        ViaturaPendencia pendencia = viaturaPendenciaRepository.findById(pendenciaId)
                .orElseThrow(() -> new NoSuchElementException("Pendencia nao encontrada."));

        validarEscopoBatalhao(usuario, pendencia.getViatura().getBatalhao().getId());
        return pendencia;
    }

    private void validarEscopoBatalhao(User usuario, Long batalhaoId) {
        if (usuario == null || usuario.getBatalhao() == null) {
            throw new AccessDeniedException("Usuario sem vinculo de batalhao.");
        }

        if (!Objects.equals(usuario.getBatalhao().getId(), batalhaoId)) {
            throw new AccessDeniedException("Voce nao pode acessar pendencias de outro batalhao.");
        }
    }
}
