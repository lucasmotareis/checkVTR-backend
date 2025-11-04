package pmto._bpm.viaturas.service;

import org.springframework.stereotype.Service;
import pmto._bpm.viaturas.auth.model.User;
import pmto._bpm.viaturas.dto.CheckListDTO;
import pmto._bpm.viaturas.dto.CheckListProblemaDTO;
import pmto._bpm.viaturas.model.CheckList;
import pmto._bpm.viaturas.model.CheckListProblema;
import pmto._bpm.viaturas.model.Problema;
import pmto._bpm.viaturas.model.Viatura;
import pmto._bpm.viaturas.repository.CheckListRepository;
import pmto._bpm.viaturas.repository.ProblemaRepository;
import pmto._bpm.viaturas.repository.ViaturaRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class CheckListService {

    private final CheckListRepository checkListRepository;
    private final ViaturaRepository viaturaRepository;
    private final ProblemaRepository problemaRepository;

    public CheckListService(CheckListRepository checkListRepository, ViaturaRepository viaturaRepository, ProblemaRepository problemaRepository) {
        this.checkListRepository = checkListRepository;
        this.viaturaRepository = viaturaRepository;
        this.problemaRepository = problemaRepository;
    }

    public CheckListDTO criar(CheckListDTO dto, User user) {
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
            checklistProblema.setChecklist(checkList); // associar o checklist
            checklistProblema.setProblema(problema);
            checklistProblema.setObservacao(problemaDTO.getObservacao());
            problemas.add(checklistProblema);
        }
        checkList.setProblemas(problemas);
        checkListRepository.save(checkList);

        return dto;
    }

    public List<CheckList> findByViaturaId(Long id) {
        return checkListRepository.findByViaturaId(id);
    }

}
