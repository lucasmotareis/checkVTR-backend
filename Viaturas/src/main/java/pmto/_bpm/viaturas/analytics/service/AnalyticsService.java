package pmto._bpm.viaturas.analytics.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import pmto._bpm.viaturas.analytics.dto.CountDTO;
import pmto._bpm.viaturas.analytics.dto.TopItemDTO;
import pmto._bpm.viaturas.analytics.dto.ViaturaCountDTO;
import pmto._bpm.viaturas.analytics.repository.AnalyticsRepository;

import java.time.Instant;
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

    public CountDTO totalProblemas(Long batalhaoId, Instant inicio, Instant fim) {
        return new CountDTO(analyticsRepository.totalProblemas(batalhaoId, inicio, fim));
    }

}
