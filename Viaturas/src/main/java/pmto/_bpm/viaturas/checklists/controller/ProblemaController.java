package pmto._bpm.viaturas.checklists.controller;

import org.springframework.web.bind.annotation.*;
import pmto._bpm.viaturas.checklists.dto.ProblemaDTO;
import pmto._bpm.viaturas.checklists.model.Problema;
import pmto._bpm.viaturas.checklists.repository.ProblemaRepository;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/problemas")
public class ProblemaController {

    private final ProblemaRepository problemaRepository;

    public ProblemaController(ProblemaRepository problemaRepository) {
        this.problemaRepository = problemaRepository;
    }

    private ProblemaDTO toDTO(Problema problema) {
        return new ProblemaDTO(
                problema.getId(),
                problema.getCategoria().name(),
                problema.getDescricao()
        );
    }

    @GetMapping
    public List<ProblemaDTO> listarTodos() {
        return problemaRepository.findAll()
                .stream()
                .sorted(Comparator
                        .comparingInt(Problema::getOrdemCategoria)
                        .thenComparing(Problema::getDescricao, String.CASE_INSENSITIVE_ORDER))
                .map(this::toDTO)
                .toList();
    }

    @GetMapping("/por-categoria")
    public Map<String, List<ProblemaDTO>> listarPorCategoria() {
        return problemaRepository.findAll().stream()
                .sorted(Comparator
                        .comparingInt(Problema::getOrdemCategoria)
                        .thenComparing(Problema::getDescricao, String.CASE_INSENSITIVE_ORDER))
                .map(this::toDTO)
                .collect(Collectors.groupingBy(
                        ProblemaDTO::categoria,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }
}
