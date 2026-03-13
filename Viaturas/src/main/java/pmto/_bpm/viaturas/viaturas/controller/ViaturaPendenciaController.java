package pmto._bpm.viaturas.viaturas.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pmto._bpm.viaturas.users.model.User;
import pmto._bpm.viaturas.viaturas.dto.PendenciaAcaoDTO;
import pmto._bpm.viaturas.viaturas.dto.ViaturaPendenciaResponseDTO;
import pmto._bpm.viaturas.viaturas.model.ViaturaPendencia;
import pmto._bpm.viaturas.viaturas.service.ViaturaPendenciaService;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/viaturas/pendencias")
public class ViaturaPendenciaController {

    private final ViaturaPendenciaService viaturaPendenciaService;

    public ViaturaPendenciaController(ViaturaPendenciaService viaturaPendenciaService) {
        this.viaturaPendenciaService = viaturaPendenciaService;
    }

    private User getAuthenticatedUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    @GetMapping("/viatura/{viaturaId}")
    public ResponseEntity<?> listarAbertasPorViatura(
            @PathVariable Long viaturaId,
            Authentication authentication
    ) {
        try {
            User user = getAuthenticatedUser(authentication);

            List<ViaturaPendenciaResponseDTO> response = viaturaPendenciaService
                    .listarAbertasPorViatura(viaturaId, user)
                    .stream()
                    .map(this::toDTO)
                    .toList();

            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PatchMapping("/{pendenciaId}/resolver")
    @PreAuthorize("hasRole('CHEFE_TRANSPORTE')")
    public ResponseEntity<?> resolverPendencia(
            @PathVariable Long pendenciaId,
            @RequestBody(required = false) PendenciaAcaoDTO dto,
            Authentication authentication
    ) {
        try {
            User user = getAuthenticatedUser(authentication);

            ViaturaPendencia pendencia = viaturaPendenciaService.resolverPendencia(
                    pendenciaId,
                    dto != null ? dto.getObservacao() : null,
                    user
            );

            return ResponseEntity.ok(toDTO(pendencia));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{pendenciaId}/descartar")
    @PreAuthorize("hasRole('CHEFE_TRANSPORTE')")
    public ResponseEntity<?> descartarPendencia(
            @PathVariable Long pendenciaId,
            @RequestBody(required = false) PendenciaAcaoDTO dto,
            Authentication authentication
    ) {
        try {
            User user = getAuthenticatedUser(authentication);

            ViaturaPendencia pendencia = viaturaPendenciaService.descartarPendencia(
                    pendenciaId,
                    dto != null ? dto.getObservacao() : null,
                    user
            );

            return ResponseEntity.ok(toDTO(pendencia));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private ViaturaPendenciaResponseDTO toDTO(ViaturaPendencia pendencia) {
        ViaturaPendenciaResponseDTO dto = new ViaturaPendenciaResponseDTO();

        dto.setId(pendencia.getId());
        dto.setViaturaId(pendencia.getViatura().getId());
        dto.setPrefixo(pendencia.getViatura().getPrefixo());

        dto.setProblemaId(pendencia.getProblema().getId());
        dto.setCategoria(pendencia.getProblema().getCategoria().name());
        dto.setProblemaNome(pendencia.getProblema().getDescricao());

        dto.setStatus(pendencia.getStatus().name());
        dto.setQtdRelatos(pendencia.getQtdRelatos());

        dto.setPrimeiraOcorrenciaEm(pendencia.getPrimeiraOcorrenciaEm());
        dto.setUltimaOcorrenciaEm(pendencia.getUltimaOcorrenciaEm());
        dto.setUltimaObservacao(pendencia.getUltimaObservacao());

        dto.setPrimeiroChecklistId(
                pendencia.getPrimeiroChecklist() != null ? pendencia.getPrimeiroChecklist().getId() : null
        );
        dto.setUltimoChecklistId(
                pendencia.getUltimoChecklist() != null ? pendencia.getUltimoChecklist().getId() : null
        );

        dto.setResolvidoPor(
                pendencia.getResolvidoPor() != null ? pendencia.getResolvidoPor().getNomeGuerra() : null
        );
        dto.setResolvidoEm(pendencia.getResolvidoEm());
        dto.setObservacaoResolucao(pendencia.getObservacaoResolucao());

        return dto;
    }
}