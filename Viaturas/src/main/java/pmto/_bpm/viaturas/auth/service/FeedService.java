package pmto._bpm.viaturas.auth.service;


import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import pmto._bpm.viaturas.auth.dto.FeedDTO;

import java.util.ArrayList;
import java.util.List;

@Service
public class FeedService{

    private final CacheManager cacheManager;
    public FeedService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void adicionarEvento(Long batalhaoId, FeedDTO feed) {
        String cacheKey = "feed::" + batalhaoId;
        List<FeedDTO> eventos = getEventos(batalhaoId);
        System.out.println("Adicionando evento ao batalhão " + batalhaoId + ": " + feed.getDescription());

        if (eventos.size() >= 10) {
            eventos.remove(0); // remove o mais antigo
        }

        eventos.add(feed);

        Cache cache = cacheManager.getCache("feed");
        if (cache != null) {
            cache.put(cacheKey, new ArrayList<>(eventos));
            System.out.println("Eventos armazenados: " + eventos);

        }
    }

    @SuppressWarnings("unchecked")
    public List<FeedDTO> getEventos(Long batalhaoId) {
        String cacheKey = "feed::" + batalhaoId;
        Cache cache = cacheManager.getCache("feed");
        if (cache != null) {
            List<FeedDTO> eventos = cache.get(cacheKey, List.class);
            if (eventos != null) {
                return new ArrayList<>(eventos);
            }
        }
        return new ArrayList<>();
    }


}
