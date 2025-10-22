package pmto._bpm.Viaturas.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.aspectj.weaver.ast.Not;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pmto._bpm.Viaturas.auth.model.User;
import pmto._bpm.Viaturas.auth.repository.UserRepository;
import pmto._bpm.Viaturas.dto.NotificationDTO;
import pmto._bpm.Viaturas.model.Notification;
import pmto._bpm.Viaturas.model.Viatura;
import pmto._bpm.Viaturas.service.NotificationService;

import java.util.List;
@RestController
@RequestMapping("/notifications")
class NotificationController {
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    NotificationController(UserRepository userRepository, NotificationService notificationService, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    private User getAuthenticatedUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    @PostMapping
    @PreAuthorize("hasRole('CHEFE_TRANSPORTE')")
    public ResponseEntity<Notification> criar(@RequestBody @Valid NotificationDTO dto, Authentication auth) {
        User user = getAuthenticatedUser(auth);
        dto.setBatalhaoId(user.getBatalhao().getId());
        Notification nova = notificationService.criar(dto);
        return ResponseEntity.ok(nova);
    }

    @GetMapping
    public ResponseEntity<List<Notification>> listarTodas(Authentication auth) {
        User user = getAuthenticatedUser(auth);
        List<Notification> notifications = notificationService.getByBatalhao(user.getBatalhao().getId());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("{id}")
    public ResponseEntity<Notification> getNotificacaoById(@PathVariable Long id, Authentication auth) {
        User user = getAuthenticatedUser(auth);
        Notification notification = notificationService.getNotificationById(id);
        if (!notification.getBatalhao().getId().equals(user.getBatalhao().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(notification);
    }



    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CHEFE_TRANSPORTE')")
    public ResponseEntity<?> atualizar( @PathVariable Long id,@RequestBody @Valid NotificationDTO dto, Authentication auth) {
        try {
            User user = getAuthenticatedUser(auth);
            Notification notification = notificationService.getNotificationById(id);
            if (!notification.getBatalhao().getId().equals(user.getBatalhao().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você não pode alterar notificações de outro batalhão.");
            }
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
