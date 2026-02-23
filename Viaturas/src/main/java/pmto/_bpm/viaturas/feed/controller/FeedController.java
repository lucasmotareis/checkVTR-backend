package pmto._bpm.viaturas.feed.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pmto._bpm.viaturas.feed.dto.FeedDTO;
import pmto._bpm.viaturas.users.model.User;
import pmto._bpm.viaturas.feed.service.FeedService;

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
