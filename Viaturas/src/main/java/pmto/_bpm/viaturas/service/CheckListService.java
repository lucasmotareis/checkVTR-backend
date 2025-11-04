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

import java.time.LocalTime;
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
            viaturaRepository.save(viatura);
        }

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
                viatura.getBatalhao().getId(),
                new FeedDTO(
                        "Check-List",
                        user.getNome_guerra() + " finalizou checklist da VTR " + viatura.getPrefixo() + " às " + LocalTime.now().withSecond(0).withNano(0)
                )
        );

        return checkListRepository.save(checkList);
    }


    public CheckListResponseDTO toDTO(CheckList checkList) {
        CheckListResponseDTO dto = new CheckListResponseDTO();
        dto.setId(checkList.getId());
        dto.setData(checkList.getData());
        dto.setKmAtual(checkList.getKmAtual());
        dto.setNomeGuerra(checkList.getUsuario().getNome_guerra());
        dto.setMatricula(checkList.getUsuario().getMatricula());
        dto.setImagens(checkList.getImagens());

        List<CheckListProblemaDTO> problemasDTO = checkList.getProblemas().stream().map(p -> {
            CheckListProblemaDTO dtoP = new CheckListProblemaDTO();
            dtoP.setProblemaId(p.getProblema().getId());
            dtoP.setObservacao(p.getObservacao());
            return dtoP;
        }).toList();

        dto.setProblemas(problemasDTO);

        return dto;
    }

    public List<CheckListResponseDTO> findByViaturaId(Long id) {
        List<CheckList> checklists  = checkListRepository.findByViaturaId(id);
        List<CheckListResponseDTO> lista = new ArrayList<>();
        for (CheckList checkList : checklists) {
            CheckListResponseDTO dto = toDTO(checkList);
            lista.add(dto);
        }
        return lista;

    }

}
