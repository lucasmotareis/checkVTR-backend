package pmto._bpm.viaturas.common.logging.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pmto._bpm.viaturas.common.logging.dto.ClientLogDTO;
import pmto._bpm.viaturas.users.model.User;

@RestController
@RequestMapping("/client-logs")
public class ClientLogController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientLogController.class);

    @PostMapping("/checklist")
    public ResponseEntity<Void> logChecklistEvent(
            @RequestBody @Valid ClientLogDTO dto,
            Authentication authentication
    ) {
        User user = getAuthenticatedUser(authentication);

        LOGGER.info(
                "client_checklist_log level={} source={} event={} clientSubmissionId={} userId={} userBatalhaoId={} data={}",
                dto.getLevel(),
                dto.getSource(),
                dto.getEvent(),
                dto.getClientSubmissionId(),
                getUserId(user),
                getUserBatalhaoId(user),
                dto.getData()
        );

        return ResponseEntity.noContent().build();
    }

    private User getAuthenticatedUser(Authentication authentication) {
        Object principal = authentication == null ? null : authentication.getPrincipal();
        return principal instanceof User ? (User) principal : null;
    }

    private Long getUserId(User user) {
        return user == null ? null : user.getId();
    }

    private Long getUserBatalhaoId(User user) {
        return user == null || user.getBatalhao() == null ? null : user.getBatalhao().getId();
    }
}
