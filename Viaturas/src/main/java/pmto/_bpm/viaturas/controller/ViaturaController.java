package pmto._bpm.viaturas.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import pmto._bpm.viaturas.auth.model.User;
import pmto._bpm.viaturas.auth.repository.UserRepository;
import pmto._bpm.viaturas.dto.ViaturaDTO;
import org.springframework.web.bind.annotation.*;
import pmto._bpm.viaturas.model.Viatura;
import pmto._bpm.viaturas.service.ViaturaService;

import java.util.List;

@RestController
public class ViaturaController {

    private final ViaturaService viaturaService;
    private final UserRepository userRepository;

    public ViaturaController(ViaturaService viaturaService, UserRepository userRepository) {
        this.viaturaService = viaturaService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    @GetMapping("viaturas")
    public ResponseEntity<List<Viatura>> getAllViaturas(Authentication auth) {
        User user = getAuthenticatedUser(auth);
        List<Viatura> viaturas = viaturaService.getByBatalhao(user.getBatalhao().getId());
        return ResponseEntity.ok(viaturas);
    }


    @GetMapping("viatura/{id}")
    public ResponseEntity<Viatura> getViaturaById(@PathVariable Long id, Authentication auth) {
        User user = getAuthenticatedUser(auth);
        Viatura viatura = viaturaService.getViaturaById(id);
        if (!viatura.getBatalhao().getId().equals(user.getBatalhao().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(viatura);
    }



    @PostMapping("viatura")
    @PreAuthorize("hasRole('CHEFE_TRANSPORTE')")
    public ResponseEntity<Viatura> createViatura(@RequestBody @Valid ViaturaDTO dto, Authentication auth) {
        User user = getAuthenticatedUser(auth);
        dto.setBatalhaoId(user.getBatalhao().getId()); // força a viatura ser do batalhão do user
        Viatura nova = viaturaService.save(dto);
        return ResponseEntity.ok(nova);
    }
    @PutMapping("viatura/{id}")
    @PreAuthorize("hasRole('CHEFE_TRANSPORTE')")
    public ResponseEntity<?> atualizarViatura(@PathVariable Long id, @RequestBody @Valid ViaturaDTO dto, Authentication auth) {
        User user = getAuthenticatedUser(auth);
        Viatura viatura = viaturaService.getViaturaById(id);

        if (!viatura.getBatalhao().getId().equals(user.getBatalhao().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você não pode alterar viaturas de outro batalhão.");
        }

        Viatura atualizada = viaturaService.atualizar(id, dto);
        return ResponseEntity.ok(atualizada);
    }



    @DeleteMapping("viatura/{id}")
    @PreAuthorize("hasRole('CHEFE_TRANSPORTE')")
    public ResponseEntity<?> deleteViatura(@PathVariable Long id, Authentication auth) {
        User user = getAuthenticatedUser(auth);
        Viatura viatura = viaturaService.getViaturaById(id);

        if (!viatura.getBatalhao().getId().equals(user.getBatalhao().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você não pode deletar viaturas de outro batalhão.");
        }

        viaturaService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
