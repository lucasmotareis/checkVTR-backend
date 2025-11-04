package pmto._bpm.viaturas.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pmto._bpm.viaturas.auth.model.User;
import pmto._bpm.viaturas.dto.CheckListDTO;
import pmto._bpm.viaturas.dto.CheckListResponseDTO;
import pmto._bpm.viaturas.model.CheckList;
import pmto._bpm.viaturas.service.CheckListService;

import java.util.List;

@RestController
@RequestMapping("/checklist")
public class CheckListController {

    private final CheckListService checkListService;

    public CheckListController(CheckListService checkListService) {
        this.checkListService = checkListService;
    }

    private User getAuthenticatedUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    @PostMapping
    public ResponseEntity<?> criarChecklist(
            @RequestBody @Valid CheckListDTO dto,
            Authentication authentication) {
        try {
            User user = getAuthenticatedUser(authentication);
            CheckList checklist = checkListService.criar(dto,user);
            CheckListResponseDTO responseDTO = checkListService.toDTO(checklist);
            return ResponseEntity.ok(responseDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao criar checklist: " + e.getMessage());
        }
    }

    @GetMapping("/viaturas/{id}/checklists")
    public List<CheckListResponseDTO> getChecklistsByViatura(@PathVariable Long id) {
        return checkListService.findByViaturaId(id);
    }
}
