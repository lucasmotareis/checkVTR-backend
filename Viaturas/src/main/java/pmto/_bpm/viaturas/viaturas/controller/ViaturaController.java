package pmto._bpm.viaturas.viaturas.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import pmto._bpm.viaturas.users.model.User;
import pmto._bpm.viaturas.viaturas.dto.ViaturaByIdDTO;
import pmto._bpm.viaturas.viaturas.dto.ViaturaComBadgeDTO;
import pmto._bpm.viaturas.viaturas.dto.ViaturaDTO;
import org.springframework.web.bind.annotation.*;
import pmto._bpm.viaturas.viaturas.service.ViaturaService;

import java.util.List;

@RestController
public class ViaturaController {

    private final ViaturaService viaturaService;

    public ViaturaController(ViaturaService viaturaService) {
        this.viaturaService = viaturaService;
    }

    private User getAuthenticatedUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    @GetMapping("viaturas")
    public ResponseEntity<List<ViaturaByIdDTO>> getAllViaturas(Authentication auth) {
        User user = getAuthenticatedUser(auth);
        return ResponseEntity.ok(viaturaService.listarViaturasDTO(user.getBatalhao().getId()));
    }


    @GetMapping("viaturas-com-badge")
    public ResponseEntity<List<ViaturaComBadgeDTO>> getAllViaturasBadge(Authentication auth) {
        User user = getAuthenticatedUser(auth);
        return ResponseEntity.ok(viaturaService.listarComBadges(user.getBatalhao().getId()));
    }


    @GetMapping("viatura/{id}")
    public ResponseEntity<ViaturaByIdDTO> getViaturaById(@PathVariable Long id, Authentication auth) {
        User user = getAuthenticatedUser(auth);
        var viatura = viaturaService.getViaturaById(id);
        if (!viatura.getBatalhao().getId().equals(user.getBatalhao().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(viaturaService.getViaturaById2(id));
    }



    @PostMapping("viatura")
    @PreAuthorize("hasRole('CHEFE_TRANSPORTE')")
    public ResponseEntity<ViaturaByIdDTO> createViatura(@RequestBody @Valid ViaturaDTO dto, Authentication auth) {
        User user = getAuthenticatedUser(auth);
        dto.setBatalhaoId(user.getBatalhao().getId()); // força a viatura ser do batalhão do user
        return ResponseEntity.ok(viaturaService.toByIdDTO(viaturaService.save(dto)));
    }

    @PutMapping("viatura/{id}")
    @PreAuthorize("hasRole('CHEFE_TRANSPORTE')")
    public ResponseEntity<?> atualizarViatura(@PathVariable Long id, @RequestBody @Valid ViaturaDTO dto, Authentication auth) {
        User user = getAuthenticatedUser(auth);
        var viatura = viaturaService.getViaturaById(id);

        if (!viatura.getBatalhao().getId().equals(user.getBatalhao().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você não pode alterar viaturas de outro batalhão.");
        }


        return ResponseEntity.ok(viaturaService.toByIdDTO(viaturaService.atualizar(id, dto)));
    }



    @DeleteMapping("viatura/{id}")
    @PreAuthorize("hasRole('CHEFE_TRANSPORTE')")
    public ResponseEntity<?> deleteViatura(@PathVariable Long id, Authentication auth) {
        User user = getAuthenticatedUser(auth);
        var viatura = viaturaService.getViaturaById(id);

        if (!viatura.getBatalhao().getId().equals(user.getBatalhao().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você não pode deletar viaturas de outro batalhão.");
        }

        viaturaService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
