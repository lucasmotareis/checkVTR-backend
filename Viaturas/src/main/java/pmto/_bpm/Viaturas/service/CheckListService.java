package pmto._bpm.Viaturas.service;

import org.springframework.stereotype.Service;
import pmto._bpm.Viaturas.dto.CheckListDTO;
import pmto._bpm.Viaturas.dto.ItemCheckListDTO;
import pmto._bpm.Viaturas.model.CheckList;
import pmto._bpm.Viaturas.model.ItemCheckList;
import pmto._bpm.Viaturas.model.Viatura;
import pmto._bpm.Viaturas.repository.CheckListRepository;
import pmto._bpm.Viaturas.repository.ViaturaRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CheckListService {

    private final CheckListRepository checkListRepository;
    private final ViaturaRepository viaturaRepository;

    public CheckListService(CheckListRepository checkListRepository, ViaturaRepository viaturaRepository) {
        this.checkListRepository = checkListRepository;
        this.viaturaRepository = viaturaRepository;
    }

    public CheckList criar(CheckListDTO dto) {
        Viatura viatura = viaturaRepository.findById(dto.getViaturaId())
                .orElseThrow(() -> new RuntimeException("Viatura não encontrada."));
        CheckList checkList = new CheckList();
        checkList.setViatura(viatura);
        checkList.setImagens(dto.getImagens());


        if (dto.getKmAtual() != null && dto.getKmAtual() > 0) {
            checkList.setKmAtual(dto.getKmAtual());
            viatura.setKm_atual(dto.getKmAtual());
            viaturaRepository.save(viatura); // ← importante!
        }



        List<ItemCheckList> itens = new ArrayList<>();
        for (ItemCheckListDTO itemDTO : dto.getItens()) {
            ItemCheckList item = new ItemCheckList();
            item.setChecklist(checkList); // ← importante!
            item.setItem(itemDTO.getItem());
            item.setTipoProblema(itemDTO.getTipoProblema());
            item.setObservacao(itemDTO.getObservacao());
            itens.add(item);
        }
        checkList.setItens(itens); // ← SETAR ANTES DE SALVAR


        return checkListRepository.save(checkList);
    }
}
