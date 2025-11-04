package pmto._bpm.viaturas.auth.dto;

import java.time.LocalDateTime;

public class FeedDTO {

    private String title;
    private String description;
    public FeedDTO(String title, String description) {
        this.title = title;
        this.description = description;
    }



    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
