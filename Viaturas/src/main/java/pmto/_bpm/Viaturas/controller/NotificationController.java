package pmto._bpm.Viaturas.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pmto._bpm.Viaturas.dto.NotificationDTO;
import pmto._bpm.Viaturas.model.Notification;
import pmto._bpm.Viaturas.service.NotificationService;

import java.util.List;
@RestController
@RequestMapping("/notifications")
class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<Notification> criar(@RequestBody @Valid NotificationDTO dto) {
        Notification nova = notificationService.criar(dto);
        return ResponseEntity.ok(nova);
    }

    @GetMapping
    public ResponseEntity<List<Notification>> listarTodas() {
        return ResponseEntity.ok(notificationService.findAll());
    }


    @PutMapping("/{id}")
    public ResponseEntity<Notification> atualizar( @PathVariable Long id,@RequestBody @Valid NotificationDTO dto) {
        try {
            Notification atualizada = notificationService.atualizar(id, dto);
            return ResponseEntity.ok(atualizada);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        try {
            notificationService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


}
