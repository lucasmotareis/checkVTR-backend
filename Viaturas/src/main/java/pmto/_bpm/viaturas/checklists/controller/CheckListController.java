package pmto._bpm.viaturas.checklists.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pmto._bpm.viaturas.users.model.User;
import pmto._bpm.viaturas.checklists.dto.CheckListDTO;
import pmto._bpm.viaturas.checklists.dto.CheckListResponseDTO;
import pmto._bpm.viaturas.checklists.model.CheckList;
import pmto._bpm.viaturas.checklists.service.CheckListService;

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

    @GetMapping
    public ResponseEntity<Page<CheckListResponseDTO>> getAllChecklists(Pageable pageable) {
        Page<CheckListResponseDTO> result = checkListService.findAll(pageable);
        return ResponseEntity.ok(result);
    }




    @GetMapping("/viaturas/{id}/checklists")
    public ResponseEntity<Page<CheckListResponseDTO>> getChecklistsByViatura(
            @PathVariable Long id,
            Pageable pageable
    ) {
        Page<CheckListResponseDTO> result = checkListService.findByViaturaId(id, pageable);
        return ResponseEntity.ok(result);
    }
}
