package pmto._bpm.viaturas.analytics.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import pmto._bpm.viaturas.analytics.dto.*;
import pmto._bpm.viaturas.analytics.repository.AnalyticsRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    public AnalyticsService(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    public List<TopItemDTO> topProblemas(Long batalhaoId, Instant inicio, Instant fim, int limit) {
        return analyticsRepository.topProblemas(
                batalhaoId, inicio, fim,
                PageRequest.of(0, Math.min(limit, 50))
        );
    }

    public List<ViaturaCountDTO> viaturasComMaisProblemas(Long batalhaoId, Instant inicio, Instant fim, int limit) {
        return analyticsRepository.viaturasComMaisProblemas(
                batalhaoId, inicio, fim,
                PageRequest.of(0, Math.min(limit, 50))
        );
    }

    public DashboardDTO  dashBoardInicial (Long batalhaoId) {
        long totalViaturas = analyticsRepository.totalViaturas(batalhaoId);
        long emManutencao = analyticsRepository.viaturasEmManutencao(batalhaoId);
        long naoVistos = analyticsRepository.checklistsNaoVistos(batalhaoId);
        long disponiveis = Math.max(0, totalViaturas - emManutencao);


        return new DashboardDTO(
                emManutencao,
                disponiveis,
                naoVistos
        );
    }

    public List<ChecklistsPorMesDTO> checklistsUltimos5Meses(Long batalhaoId) {
        ZoneId zone = ZoneId.of("America/Araguaina");

        LocalDate primeiroDiaDoMesAtual = LocalDate.now(zone).withDayOfMonth(1);
        LocalDate inicioLocal = primeiroDiaDoMesAtual.minusMonths(5);  // pega 5 meses antes
        LocalDate fimLocal = primeiroDiaDoMesAtual.plusMonths(1);      // inclui mês atual inteiro

        Instant inicio = inicioLocal.atStartOfDay(zone).toInstant();
        Instant fim = fimLocal.atStartOfDay(zone).toInstant();

        List<Object[]> rows = analyticsRepository.checklistsPorAnoMesRaw(batalhaoId, inicio, fim);

        return rows.stream()
                .map(r -> new ChecklistsPorMesDTO(
                        ((Number) r[0]).intValue(), // ano
                        ((Number) r[1]).intValue(), // mes
                        ((Number) r[2]).longValue() // total
                ))
                .toList();
    }

}
