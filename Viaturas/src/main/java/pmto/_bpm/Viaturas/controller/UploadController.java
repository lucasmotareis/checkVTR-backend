package pmto._bpm.Viaturas.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pmto._bpm.Viaturas.dto.ChecklistUploadMetadata;
import pmto._bpm.Viaturas.dto.ChecklistUploadRequestDTO;
import pmto._bpm.Viaturas.model.CheckList;
import pmto._bpm.Viaturas.service.AwsS3Service;

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
