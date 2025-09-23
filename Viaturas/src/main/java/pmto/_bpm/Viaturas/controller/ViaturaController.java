package pmto._bpm.Viaturas.controller;

import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import pmto._bpm.Viaturas.dto.ViaturaDTO;
import org.springframework.web.bind.annotation.*;
import pmto._bpm.Viaturas.model.Viatura;
import pmto._bpm.Viaturas.service.ViaturaService;

import java.util.List;

@RestController
public class ViaturaController {

    private final ViaturaService viaturaService;

    public ViaturaController(ViaturaService viaturaService) {
        this.viaturaService = viaturaService;
    }

    @GetMapping("/viaturas")
    public List<Viatura> getAllViaturas() {
        return viaturaService.getAll();
    }

    @GetMapping("viatura/{id}")
    public Viatura getViaturaById(@PathVariable Long id){
        return viaturaService.getViaturaById(id);
    }

    @PostMapping("/viatura")
    public ResponseEntity<Viatura> createViatura(@RequestBody @Valid ViaturaDTO dto) {
        Viatura nova = viaturaService.save(dto);
        return ResponseEntity.ok(nova);
    }

    @PutMapping("viatura/{id}")
    public ResponseEntity<Viatura> atualizarViatura(
            @PathVariable Long id,
            @RequestBody @Valid ViaturaDTO dto
    ) {
        Viatura atualizada = viaturaService.atualizar(id, dto);
        return ResponseEntity.ok(atualizada);
    }

    @DeleteMapping("viatura/{id}")
    public ResponseEntity<Void> deleteViatura(@PathVariable Long id) {
        viaturaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
