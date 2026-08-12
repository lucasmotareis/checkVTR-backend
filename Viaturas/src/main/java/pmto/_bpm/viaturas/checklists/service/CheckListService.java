package pmto._bpm.viaturas.checklists.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import pmto._bpm.viaturas.checklists.dto.CheckListDTO;
import pmto._bpm.viaturas.checklists.dto.CheckListProblemaRequestDTO;
import pmto._bpm.viaturas.checklists.dto.CheckListProblemaResponseDTO;
import pmto._bpm.viaturas.checklists.dto.CheckListResponseDTO;
import pmto._bpm.viaturas.checklists.model.CheckList;
import pmto._bpm.viaturas.checklists.model.CheckListProblema;
import pmto._bpm.viaturas.checklists.model.Problema;
import pmto._bpm.viaturas.checklists.repository.CheckListRepository;
import pmto._bpm.viaturas.checklists.repository.ProblemaRepository;
import pmto._bpm.viaturas.feed.dto.FeedDTO;
import pmto._bpm.viaturas.feed.service.FeedService;
import pmto._bpm.viaturas.users.model.User;
import pmto._bpm.viaturas.viaturas.model.Viatura;
import pmto._bpm.viaturas.viaturas.repository.ViaturaRepository;
import pmto._bpm.viaturas.viaturas.service.ViaturaPendenciaService;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CheckListService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckListService.class);

    private final CheckListRepository checkListRepository;
    private final ViaturaRepository viaturaRepository;
    private final ProblemaRepository problemaRepository;
    private final FeedService feedService;
    private final ViaturaPendenciaService viaturaPendenciaService;

    public CheckListService(FeedService feedService,
                            CheckListRepository checkListRepository,
                            ViaturaRepository viaturaRepository,
                            ProblemaRepository problemaRepository, ViaturaPendenciaService viaturaPencenciaService) {
        this.feedService = feedService;
        this.checkListRepository = checkListRepository;
        this.viaturaRepository = viaturaRepository;
        this.problemaRepository = problemaRepository;
        this.viaturaPendenciaService = viaturaPencenciaService;
    }

    @Transactional
    public CheckList criar(CheckListDTO dto, User user) {
        long startedAt = System.currentTimeMillis();
        String clientSubmissionId = dto.getClientSubmissionId();

        LOGGER.info(
                "checklist_create_started clientSubmissionId={} userId={} userBatalhaoId={} viaturaId={} imagensCount={} problemasCount={}",
                clientSubmissionId,
                user.getId(),
                user.getBatalhao() == null ? null : user.getBatalhao().getId(),
                dto.getViaturaId(),
                sizeOf(dto.getImagens()),
                sizeOf(dto.getProblemas())
        );

        Viatura viatura = viaturaRepository.findById(dto.getViaturaId())
                .orElseThrow(() -> new NoSuchElementException("Viatura nao encontrada."));

        LOGGER.info(
                "checklist_vehicle_loaded clientSubmissionId={} viaturaId={} viaturaBatalhaoId={} prefixo={} placa={}",
                clientSubmissionId,
                viatura.getId(),
                viatura.getBatalhao() == null ? null : viatura.getBatalhao().getId(),
                viatura.getPrefixo(),
                viatura.getPlaca()
        );

        if (!viatura.getBatalhao().getId().equals(user.getBatalhao().getId())) {
            LOGGER.warn(
                    "checklist_batalhao_denied clientSubmissionId={} userBatalhaoId={} viaturaBatalhaoId={} viaturaId={}",
                    clientSubmissionId,
                    user.getBatalhao() == null ? null : user.getBatalhao().getId(),
                    viatura.getBatalhao() == null ? null : viatura.getBatalhao().getId(),
                    viatura.getId()
            );
            throw new AccessDeniedException("Voce nao pode criar checklist para viatura de outro batalhao.");
        }

        CheckList checkList = new CheckList();
        checkList.setViatura(viatura);
        checkList.setImagens(dto.getImagens());
        checkList.setUsuario(user);

        if (dto.getKmAtual() != null && dto.getKmAtual() > 0) {
            checkList.setKmAtual(dto.getKmAtual());
            viatura.setKmAtual(dto.getKmAtual());
        }

        if (dto.getKmRevisao() != null && dto.getKmRevisao() > 0) {
            viatura.setKmRevisao(dto.getKmRevisao());
        }

        // Se você já adicionou esse campo
        if (dto.getCombustivelAtualPercentual() != null) {
            checkList.setCombustivelAtualPercentual(dto.getCombustivelAtualPercentual());
            viatura.setCombustivelAtualPercentual(dto.getCombustivelAtualPercentual());
        }

        viaturaRepository.save(viatura);


        checkList.setKmRevisao(dto.getKmRevisao());

        List<CheckListProblema> problemas = new ArrayList<>();
        for (CheckListProblemaRequestDTO problemaDTO : dto.getProblemas()) {
            Problema problema = problemaRepository.findById(problemaDTO.getProblemaId())
                    .orElseThrow(() -> new NoSuchElementException("Problema nao encontrado: ID " + problemaDTO.getProblemaId()));
            CheckListProblema checklistProblema = new CheckListProblema();
            checklistProblema.setChecklist(checkList);
            checklistProblema.setProblema(problema);
            checklistProblema.setObservacao(problemaDTO.getObservacao());
            problemas.add(checklistProblema);
        }
        checkList.setProblemas(problemas);

        CheckList checkListSalvo = checkListRepository.save(checkList);
        LOGGER.info(
                "checklist_persisted clientSubmissionId={} checklistId={} imagensCount={} problemasCount={}",
                clientSubmissionId,
                checkListSalvo.getId(),
                sizeOf(checkListSalvo.getImagens()),
                sizeOf(checkListSalvo.getProblemas())
        );

        viaturaPendenciaService.registrarPendenciasCriticas(checkListSalvo);
        LOGGER.info(
                "checklist_pending_issues_registered clientSubmissionId={} checklistId={}",
                clientSubmissionId,
                checkListSalvo.getId()
        );

        feedService.adicionarEvento(
                user.getBatalhao().getId(),
                new FeedDTO(
                        "Check-List",
                        user.getNomeGuerra() + " finalizou checklist da VTR " + viatura.getPrefixo() + " as " +
                                LocalTime.now(ZoneId.of("America/Sao_Paulo")).withSecond(0).withNano(0)
                        )
        );
        LOGGER.info(
                "checklist_feed_event_added clientSubmissionId={} checklistId={} batalhaoId={}",
                clientSubmissionId,
                checkListSalvo.getId(),
                user.getBatalhao().getId()
        );

        LOGGER.info(
                "checklist_create_completed clientSubmissionId={} checklistId={} durationMs={}",
                clientSubmissionId,
                checkListSalvo.getId(),
                System.currentTimeMillis() - startedAt
        );

        return checkListSalvo;
    }

    private int sizeOf(List<?> list) {
        return list == null ? 0 : list.size();
    }

    public CheckListResponseDTO toDTO(CheckList checkList) {
        CheckListResponseDTO dto = new CheckListResponseDTO();
        dto.setId(checkList.getId());
        dto.setData(checkList.getData());
        dto.setKmAtual(checkList.getKmAtual());
        dto.setPrefixo(checkList.getViatura().getPrefixo());
        dto.setKmRevisao(checkList.getKmRevisao());
        dto.setNomeGuerra(checkList.getUsuario().getNomeGuerra());
        dto.setMatricula(checkList.getUsuario().getMatricula());
        dto.setImagens(checkList.getImagens());
        dto.setVistoPeloChefe(checkList.isVistoPeloChefe());
        dto.setVistoPeloChefeEm(checkList.getVistoPeloChefeEm());

        List<CheckListProblemaResponseDTO> problemasDTO = checkList.getProblemas().stream().map(p -> {
            CheckListProblemaResponseDTO dtoP = new CheckListProblemaResponseDTO();
            dtoP.setProblemaId(p.getProblema().getId());
            dtoP.setCategoria(p.getProblema().getCategoria().name());
            dtoP.setProblemaNome(p.getProblema().getDescricao());
            dtoP.setObservacao(p.getObservacao());
            return dtoP;
        }).toList();

        dto.setProblemas(problemasDTO);
        return dto;
    }

    public Page<CheckListResponseDTO> findAll(Pageable pageable) {
        Page<CheckList> page = checkListRepository.findAll(pageable);
        return page.map(this::toDTO);
    }

    public Page<CheckListResponseDTO> findByViaturaId(Long viaturaId, Pageable pageable, User user) {
        Viatura viatura = viaturaRepository.findById(viaturaId)
                .orElseThrow(() -> new NoSuchElementException("Viatura nao encontrada."));

        if (!viatura.getBatalhao().getId().equals(user.getBatalhao().getId())) {
            throw new AccessDeniedException("Voce nao pode visualizar checklist de viatura de outro batalhao.");
        }

        Page<CheckList> page = checkListRepository.findByViaturaId(viaturaId, pageable);
        return page.map(this::toDTO);
    }

    @Transactional
    public void marcarVistoPeloChefe(Long checklistId) {
        CheckList c = checkListRepository.findById(checklistId)
                .orElseThrow(() -> new NoSuchElementException("Checklist nao encontrado"));

        if (!c.isVistoPeloChefe()) {
            c.setVistoPeloChefe(true);
            c.setVistoPeloChefeEm(Instant.now());
        }
    }
}
