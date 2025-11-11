package pmto._bpm.viaturas.service;

import org.springframework.stereotype.Service;
import pmto._bpm.viaturas.auth.dto.FeedDTO;
import pmto._bpm.viaturas.auth.model.User;
import pmto._bpm.viaturas.auth.service.FeedService;
import pmto._bpm.viaturas.dto.CheckListResponseDTO;
import pmto._bpm.viaturas.dto.CheckListDTO;
import pmto._bpm.viaturas.dto.CheckListProblemaDTO;
import pmto._bpm.viaturas.model.CheckList;
import pmto._bpm.viaturas.model.CheckListProblema;
import pmto._bpm.viaturas.model.Problema;
import pmto._bpm.viaturas.model.Viatura;
import pmto._bpm.viaturas.repository.CheckListRepository;
import pmto._bpm.viaturas.repository.ProblemaRepository;
import pmto._bpm.viaturas.repository.ViaturaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CheckListService {

    private final CheckListRepository checkListRepository;
    private final ViaturaRepository viaturaRepository;
    private final ProblemaRepository problemaRepository;
    private final FeedService feedService;

    public CheckListService(FeedService feedService, CheckListRepository checkListRepository, ViaturaRepository viaturaRepository, ProblemaRepository problemaRepository) {
        this.feedService = feedService;
        this.checkListRepository = checkListRepository;
        this.viaturaRepository = viaturaRepository;
        this.problemaRepository = problemaRepository;
    }

    public CheckList criar(CheckListDTO dto, User user) {
        Viatura viatura = viaturaRepository.findById(dto.getViaturaId())
                .orElseThrow(() -> new RuntimeException("Viatura não encontrada."));
        CheckList checkList = new CheckList();
        checkList.setViatura(viatura);
        checkList.setImagens(dto.getImagens());
        checkList.setUsuario(user);
        if (dto.getKmAtual() != null && dto.getKmAtual() > 0) {
            checkList.setKmAtual(dto.getKmAtual());
            viatura.setKm_atual(dto.getKmAtual());
            viatura.setKm_revisao(dto.getKmRevisao());
            viaturaRepository.save(viatura);
        }
        checkList.setKmRevisao(dto.getKmRevisao());

        List<CheckListProblema> problemas = new ArrayList<>();
        for (CheckListProblemaDTO problemaDTO : dto.getProblemas()) {
            Problema problema = problemaRepository.findById(problemaDTO.getProblemaId())
                    .orElseThrow(() -> new RuntimeException("Problema não encontrado: ID " + problemaDTO.getProblemaId()));
            CheckListProblema checklistProblema = new CheckListProblema();
            checklistProblema.setChecklist(checkList);
            checklistProblema.setProblema(problema);
            checklistProblema.setObservacao(problemaDTO.getObservacao());
            problemas.add(checklistProblema);
        }
        checkList.setProblemas(problemas);

        feedService.adicionarEvento(
                user.getBatalhao().getId(),
                new FeedDTO(
                        "Check-List",
                        user.getNome_guerra() + " finalizou checklist da VTR " + viatura.getPrefixo() + " às " + LocalTime.now(ZoneId.of("America/Sao_Paulo")).withSecond(0).withNano(0)
                )
        );

        return checkListRepository.save(checkList);
    }


    public CheckListResponseDTO toDTO(CheckList checkList) {
        CheckListResponseDTO dto = new CheckListResponseDTO();
        dto.setId(checkList.getId());
        dto.setData(checkList.getData());
        dto.setKmAtual(checkList.getKmAtual());
        dto.setKmRevisao(checkList.getKmRevisao());
        dto.setNomeGuerra(checkList.getUsuario().getNome_guerra());
        dto.setMatricula(checkList.getUsuario().getMatricula());
        dto.setImagens(checkList.getImagens());

        List<CheckListProblemaDTO> problemasDTO = checkList.getProblemas().stream().map(p -> {
            CheckListProblemaDTO dtoP = new CheckListProblemaDTO();
            dtoP.setProblemaId(p.getProblema().getId());
            dtoP.setCategoria(p.getProblema().getCategoria());
            dtoP.setProblemaNome(p.getProblema().getDescricao());
            dtoP.setObservacao(p.getObservacao());
            return dtoP;
        }).toList();

        dto.setProblemas(problemasDTO);

        return dto;
    }

    public Page<CheckListResponseDTO> findAll(Pageable pageable) {
        Page<CheckList> page = checkListRepository.findAll(pageable);
        return page.map(this::toDTO); // converte cada CheckList para DTO
    }

    public Page<CheckListResponseDTO> findByViaturaId(Long id, Pageable pageable) {
        Page<CheckList> page = checkListRepository.findByViaturaId(id, pageable);

        return page.map(this::toDTO); // Mapeia cada entidade para DTO

    }

}
