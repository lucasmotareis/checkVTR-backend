package pmto._bpm.Viaturas.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pmto._bpm.Viaturas.dto.CheckListDTO;
import pmto._bpm.Viaturas.model.CheckList;
import pmto._bpm.Viaturas.service.CheckListService;

import java.util.List;

@RestController
@RequestMapping("/checklist")
public class CheckListController {

    private final CheckListService checkListService;

    public CheckListController(CheckListService checkListService) {
        this.checkListService = checkListService;
    }


    @PostMapping
    public ResponseEntity<?> criarChecklist(@RequestBody @Valid CheckListDTO dto) {
        try {
            CheckList checklist = checkListService.criar(dto);
            return ResponseEntity.ok(checklist);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao criar checklist: " + e.getMessage());
        }
    }

    @GetMapping("/viaturas/{id}/checklists")
    public List<CheckList> getChecklistsByViatura(@PathVariable Long id) {
        return checkListService.findByViaturaId(id);
    }
}
