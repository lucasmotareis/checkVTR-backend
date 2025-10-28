package pmto._bpm.viaturas.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pmto._bpm.viaturas.dto.ChecklistUploadRequestDTO;
import pmto._bpm.viaturas.service.AwsS3Service;

import java.util.List;

@RestController
@RequestMapping("/upload")
public class UploadController {

    private final AwsS3Service awsS3Service;

    public UploadController(AwsS3Service awsS3Service) {
        this.awsS3Service = awsS3Service;
    }

    @PostMapping("/presigned-urls")
    public ResponseEntity<List<String>> generateUploadUrl(@RequestBody ChecklistUploadRequestDTO request) {
        List<String> urls= awsS3Service.generatePresignedUrls(request);
        return ResponseEntity.ok(urls);
    }
}
