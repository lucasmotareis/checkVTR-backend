package pmto._bpm.viaturas.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pmto._bpm.viaturas.auth.dto.FeedDTO;
import pmto._bpm.viaturas.auth.model.User;
import pmto._bpm.viaturas.auth.service.FeedService;
import pmto._bpm.viaturas.service.CheckListService;

import java.util.List;

@RestController
public class FeedController {

    private final  FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    private User getAuthenticatedUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    @GetMapping("/feed")
    public ResponseEntity<List<FeedDTO>> getFeed(Authentication auth) {
        User user = getAuthenticatedUser(auth);
        Long userBatalhao = user.getBatalhao().getId();
        List<FeedDTO> feed = feedService.getEventos(userBatalhao);
        return ResponseEntity.ok(feed);
    }

    
}
