package pmto._bpm.viaturas.controller;

import org.springframework.web.bind.annotation.*;
import pmto._bpm.viaturas.model.Problema;
import pmto._bpm.viaturas.repository.ProblemaRepository;

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

    @GetMapping
    public List<Problema> listarTodos() {
        return problemaRepository.findAll();
    }

    @GetMapping("/por-categoria")
    public Map<String, List<Problema>> listarPorCategoria() {
        return problemaRepository.findAll().stream()
                .collect(Collectors.groupingBy(Problema::getCategoria));
    }
}
