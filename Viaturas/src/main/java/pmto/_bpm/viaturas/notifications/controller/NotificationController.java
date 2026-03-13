package pmto._bpm.viaturas.notifications.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pmto._bpm.viaturas.notifications.dto.NaoLidasDTO;
import pmto._bpm.viaturas.notifications.dto.NotificacaoItemDTO;
import pmto._bpm.viaturas.notifications.dto.NotificacaoStatsDTO;
import pmto._bpm.viaturas.notifications.dto.NotificationResponseDTO;
import pmto._bpm.viaturas.users.model.User;
import pmto._bpm.viaturas.users.repository.UserRepository;
import pmto._bpm.viaturas.notifications.dto.CreateNotificationDTO;
import pmto._bpm.viaturas.notifications.model.Notification;
import pmto._bpm.viaturas.notifications.service.NotificationService;

import java.time.Instant;

@RestController
@RequestMapping("/notifications")
class NotificationController {
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    NotificationController(UserRepository userRepository, NotificationService notificationService) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    @PostMapping
    @PreAuthorize("hasRole('CHEFE_TRANSPORTE')")
    public ResponseEntity<NotificationResponseDTO> criar(@RequestBody @Valid CreateNotificationDTO dto, Authentication auth) {
        User user = getAuthenticatedUser(auth);
        dto.setBatalhaoId(user.getBatalhao().getId());
        Notification nova = notificationService.criar(dto);
        return ResponseEntity.ok(notificationService.toResponseDTO(nova));
    }

    @GetMapping("/unread-count")
    public NaoLidasDTO unreadCount(Authentication auth) {
        User user = getAuthenticatedUser(auth);
        return new NaoLidasDTO(notificationService.notificacoesNaoLidas(user));
    }

    @GetMapping("/stats")
    public NotificacaoStatsDTO stats(Authentication auth) {
        User user = getAuthenticatedUser(auth);
        return notificationService.getStats(user.getBatalhao().getId());
    }


    @GetMapping
    public ResponseEntity<Page<NotificacaoItemDTO>> listar(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ) {
        User user = getAuthenticatedUser(auth);

        // sort padrão: mais recentes primeiro
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dataCriacao"));

        return ResponseEntity.ok(notificationService.listaNotificacaoPaginado(user, pageable));
    }

    @GetMapping("{id}")
    public ResponseEntity<NotificationResponseDTO> getNotificacaoById(@PathVariable Long id, Authentication auth) {
        User user = getAuthenticatedUser(auth);
        Notification notification = notificationService.getNotificationById(id);
        if (!notification.getBatalhao().getId().equals(user.getBatalhao().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(notificationService.toResponseDTO(notification));
    }



    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CHEFE_TRANSPORTE')")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody @Valid CreateNotificationDTO dto, Authentication auth) {
        try {
            User user = getAuthenticatedUser(auth);
            Notification notification = notificationService.getNotificationById(id);
            if (!notification.getBatalhao().getId().equals(user.getBatalhao().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você não pode alterar notificações de outro batalhão.");
            }
            Notification atualizada = notificationService.atualizar(id, dto);
            return ResponseEntity.ok(notificationService.toResponseDTO(atualizada));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CHEFE_TRANSPORTE')")
    public ResponseEntity<Void> deletar(@PathVariable Long id, Authentication auth) {
        try {
            User user = getAuthenticatedUser(auth);
            Notification notification = notificationService.getNotificationById(id);
            if (!notification.getBatalhao().getId().equals(user.getBatalhao().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            notificationService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/me/visto")
    public ResponseEntity<Void> vistoNotificacao(Authentication auth) {
        User user = getAuthenticatedUser(auth);
        user.setUltimaNotificacaoVista(Instant.now());
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }



}
