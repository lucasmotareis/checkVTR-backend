package pmto._bpm.viaturas.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pmto._bpm.viaturas.auth.dto.FeedDTO;
import pmto._bpm.viaturas.auth.service.FeedService;
import pmto._bpm.viaturas.service.CheckListService;

import java.util.List;

@RestController
public class FeedController {

    private final  FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping("/feed")
    public ResponseEntity<List<FeedDTO>> getFeed(@RequestParam Long batalhaoId) {
        List<FeedDTO> feed = feedService.getEventos(batalhaoId);
        return ResponseEntity.ok(feed);
    }

    
}
