package pmto._bpm.viaturas.analytics.controller;


import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pmto._bpm.viaturas.analytics.dto.ChecklistsPorMesDTO;
import pmto._bpm.viaturas.analytics.dto.Periodo;
import pmto._bpm.viaturas.analytics.dto.TopItemDTO;
import pmto._bpm.viaturas.analytics.dto.ViaturaCountDTO;
import pmto._bpm.viaturas.analytics.service.AnalyticsService;
import pmto._bpm.viaturas.users.model.User;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }
    private User getAuthenticatedUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    // Ex: /analytics/top-problemas?limit=10&inicio=2026-01-01T00:00:00Z&fim=2026-02-01T00:00:00Z
    @GetMapping("/top-problemas")
    public List<TopItemDTO> topProblemas(
            Authentication auth,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Instant inicio,
            @RequestParam(required = false) Instant fim
    ) {
        User user = getAuthenticatedUser(auth);

        Periodo periodo = Periodo.of(inicio, fim);

        return analyticsService.topProblemas(user.getBatalhao().getId(), periodo.inicio(), periodo.fim(), limit);
    }

    @GetMapping("/viaturas-mais-problemas")
    public List<ViaturaCountDTO> viaturasMaisProblemas(
            Authentication auth,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Instant inicio,
            @RequestParam(required = false) Instant fim
    ) {
        User user = getAuthenticatedUser(auth);

        Periodo periodo = Periodo.of(inicio, fim);

        return analyticsService.viaturasComMaisProblemas(
                user.getBatalhao().getId(),
                periodo.inicio(),
                periodo.fim(),
                limit
        );
    }


    @GetMapping("/checklists-mensais")
    public List<ChecklistsPorMesDTO> checklistsUltimos5Meses(Authentication auth) {
        User user = getAuthenticatedUser(auth);
        return analyticsService.checklistsUltimos5Meses(user.getBatalhao().getId());
    }
}
