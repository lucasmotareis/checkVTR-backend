package pmto._bpm.viaturas.checklists.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pmto._bpm.viaturas.users.model.User;
import pmto._bpm.viaturas.checklists.dto.CheckListDTO;
import pmto._bpm.viaturas.checklists.dto.CheckListResponseDTO;
import pmto._bpm.viaturas.checklists.model.CheckList;
import pmto._bpm.viaturas.checklists.service.CheckListService;
import pmto._bpm.viaturas.viaturas.service.ViaturaService;

@RestController
@RequestMapping("/checklist")
public class CheckListController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckListController.class);

    private final CheckListService checkListService;
    private final ViaturaService viaturaService;

    public CheckListController(CheckListService checkListService, ViaturaService viaturaService) {
        this.checkListService = checkListService;
        this.viaturaService = viaturaService;
    }



    private User getAuthenticatedUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    @PostMapping
    public ResponseEntity<?> criarChecklist(
            @RequestBody @Valid CheckListDTO dto,
            Authentication authentication) {
        long startedAt = System.currentTimeMillis();
        String clientSubmissionId = dto == null ? null : dto.getClientSubmissionId();
        User user = null;

        try {
            user = getAuthenticatedUser(authentication);
            LOGGER.info(
                    "checklist_post_started clientSubmissionId={} userId={} userBatalhaoId={} viaturaId={} imagensCount={} problemasCount={} kmAtual={} kmRevisao={} combustivelAtualPercentual={}",
                    clientSubmissionId,
                    user.getId(),
                    user.getBatalhao() == null ? null : user.getBatalhao().getId(),
                    dto.getViaturaId(),
                    sizeOf(dto.getImagens()),
                    sizeOf(dto.getProblemas()),
                    dto.getKmAtual(),
                    dto.getKmRevisao(),
                    dto.getCombustivelAtualPercentual()
            );

            CheckList checklist = checkListService.criar(dto,user);
            CheckListResponseDTO responseDTO = checkListService.toDTO(checklist);

            LOGGER.info(
                    "checklist_post_succeeded clientSubmissionId={} checklistId={} durationMs={}",
                    clientSubmissionId,
                    checklist.getId(),
                    System.currentTimeMillis() - startedAt
            );
            return ResponseEntity.ok(responseDTO);
        } catch (AccessDeniedException e) {
            LOGGER.warn(
                    "checklist_post_denied clientSubmissionId={} userId={} durationMs={} error={}",
                    clientSubmissionId,
                    getUserId(user),
                    System.currentTimeMillis() - startedAt,
                    e.getMessage()
            );
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.warn(
                    "checklist_post_bad_request clientSubmissionId={} userId={} durationMs={} error={}",
                    clientSubmissionId,
                    getUserId(user),
                    System.currentTimeMillis() - startedAt,
                    e.getMessage()
            );
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            LOGGER.error(
                    "checklist_post_failed clientSubmissionId={} userId={} durationMs={} error={}",
                    clientSubmissionId,
                    getUserId(user),
                    System.currentTimeMillis() - startedAt,
                    e.getMessage(),
                    e
            );
            return ResponseEntity.internalServerError().body("Erro ao criar checklist: " + e.getMessage());
        }
    }

    private int sizeOf(java.util.Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    private Long getUserId(User user) {
        return user == null ? null : user.getId();
    }

    @GetMapping
    public ResponseEntity<Page<CheckListResponseDTO>> getAllChecklists(Pageable pageable) {
        Page<CheckListResponseDTO> result = checkListService.findAll(pageable);
        return ResponseEntity.ok(result);
    }


    @PostMapping("/{checklistId}/visto")
    @PreAuthorize("hasRole('CHEFE_TRANSPORTE')")
    public ResponseEntity<Void> marcarChecklistVisto(
            @PathVariable Long checklistId
    ) {
        checkListService.marcarVistoPeloChefe(checklistId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/viaturas/{id}/checklists")
    public ResponseEntity<Page<CheckListResponseDTO>> getChecklistsByViatura(
            @PathVariable Long id,
            Authentication authentication,
            Pageable pageable
    ) {
        try {
            User user = getAuthenticatedUser(authentication);
            Page<CheckListResponseDTO> result = checkListService.findByViaturaId(id, pageable, user);
            return ResponseEntity.ok(result);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).build();
        }
    }
}
