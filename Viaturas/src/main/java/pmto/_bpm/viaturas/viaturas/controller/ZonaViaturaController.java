package pmto._bpm.viaturas.viaturas.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pmto._bpm.viaturas.users.model.User;
import pmto._bpm.viaturas.viaturas.dto.ReordenarZonaViaturaDTO;
import pmto._bpm.viaturas.viaturas.dto.ZonaViaturaCidadeDTO;
import pmto._bpm.viaturas.viaturas.dto.ZonaViaturaCidadeResponseDTO;
import pmto._bpm.viaturas.viaturas.dto.ZonaViaturaDTO;
import pmto._bpm.viaturas.viaturas.dto.ZonaViaturaQuadroDTO;
import pmto._bpm.viaturas.viaturas.dto.ZonaViaturaResponseDTO;
import pmto._bpm.viaturas.viaturas.service.ZonaViaturaService;

@RestController
@RequestMapping("viaturas-zonas")
@PreAuthorize("hasRole('CHEFE_TRANSPORTE')")
public class ZonaViaturaController {

    private final ZonaViaturaService zonaViaturaService;

    public ZonaViaturaController(ZonaViaturaService zonaViaturaService) {
        this.zonaViaturaService = zonaViaturaService;
    }

    private User getAuthenticatedUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    @GetMapping("quadro")
    public ResponseEntity<List<ZonaViaturaQuadroDTO>> getQuadro(Authentication auth) {
        return ResponseEntity.ok(zonaViaturaService.listarQuadro(getAuthenticatedUser(auth)));
    }

    @PostMapping
    public ResponseEntity<ZonaViaturaResponseDTO> createZona(
            @RequestBody @Valid ZonaViaturaDTO dto,
            Authentication auth
    ) {
        return ResponseEntity.ok(zonaViaturaService.criar(dto, getAuthenticatedUser(auth)));
    }

    @PutMapping("{id}")
    public ResponseEntity<ZonaViaturaResponseDTO> updateZona(
            @PathVariable Long id,
            @RequestBody @Valid ZonaViaturaDTO dto,
            Authentication auth
    ) {
        return ResponseEntity.ok(zonaViaturaService.atualizar(id, dto, getAuthenticatedUser(auth)));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteZona(@PathVariable Long id, Authentication auth) {
        zonaViaturaService.deletar(id, getAuthenticatedUser(auth));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("ordem")
    public ResponseEntity<Void> reorderZonas(
            @RequestBody @Valid ReordenarZonaViaturaDTO dto,
            Authentication auth
    ) {
        zonaViaturaService.reordenar(dto, getAuthenticatedUser(auth));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("{zonaId}/cidades")
    public ResponseEntity<ZonaViaturaCidadeResponseDTO> createCidade(
            @PathVariable Long zonaId,
            @RequestBody @Valid ZonaViaturaCidadeDTO dto,
            Authentication auth
    ) {
        return ResponseEntity.ok(zonaViaturaService.criarCidade(zonaId, dto, getAuthenticatedUser(auth)));
    }

    @PutMapping("cidades/{cidadeId}")
    public ResponseEntity<ZonaViaturaCidadeResponseDTO> updateCidade(
            @PathVariable Long cidadeId,
            @RequestBody @Valid ZonaViaturaCidadeDTO dto,
            Authentication auth
    ) {
        return ResponseEntity.ok(zonaViaturaService.atualizarCidade(cidadeId, dto, getAuthenticatedUser(auth)));
    }

    @DeleteMapping("cidades/{cidadeId}")
    public ResponseEntity<Void> deleteCidade(@PathVariable Long cidadeId, Authentication auth) {
        zonaViaturaService.deletarCidade(cidadeId, getAuthenticatedUser(auth));
        return ResponseEntity.noContent().build();
    }
}
