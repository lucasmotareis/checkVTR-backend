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
        System.out.println("Adicionando evento ao batalhão " + batalhaoId + ": " + feed.getDescription());

        Cache cache = cacheManager.getCache("ultimosChecklists");

        if (cache == null) {
            System.out.println("Cache 'ultimosChecklists' não encontrado.");
            return;
        }
        List<FeedDTO> eventos = cache.get(batalhaoId, List.class);

        if (eventos == null) {
            eventos = new ArrayList<>(); // Se não existir ainda, cria nova lista
        }

        if (eventos.size() >= 10) {
            eventos.remove(0); // Remove o mais antigo
        }

        eventos.add(feed);

        cache.put(batalhaoId, eventos);

        System.out.println("Eventos armazenados: " + eventos);



    }



    @SuppressWarnings("unchecked")
    public List<FeedDTO> getEventos(Long batalhaoId) {
        Cache cache = cacheManager.getCache("ultimosChecklists");
        if (cache != null) {
            List<FeedDTO> eventos = cache.get(batalhaoId, List.class);
            if (eventos != null) {
                return new ArrayList<>(eventos);
            }
        }
        return new ArrayList<>();
    }



}
