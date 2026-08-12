package pmto._bpm.viaturas.storage.s3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pmto._bpm.viaturas.checklists.dto.ChecklistUploadMetadata;
import pmto._bpm.viaturas.checklists.dto.ChecklistUploadRequestDTO;
import pmto._bpm.viaturas.users.model.User;

import java.util.List;

@RestController
@RequestMapping("/upload")
public class UploadController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadController.class);

    private final AwsS3Service awsS3Service;

    public UploadController(AwsS3Service awsS3Service) {
        this.awsS3Service = awsS3Service;
    }

    @PostMapping("/presigned-urls")
    public ResponseEntity<List<String>> generateUploadUrl(
            @RequestBody ChecklistUploadRequestDTO request,
            Authentication authentication
    ) {
        long startedAt = System.currentTimeMillis();
        ChecklistUploadMetadata metadata = getMetadata(request);
        User user = getAuthenticatedUser(authentication);
        String clientSubmissionId = getClientSubmissionId(metadata);
        int quantidade = request == null ? 0 : request.getQuantidade();

        LOGGER.info(
                "presigned_urls_started clientSubmissionId={} quantidade={} prefixo={} placa={} batalhao={} matricula={} userId={} userBatalhaoId={}",
                clientSubmissionId,
                quantidade,
                getPrefixo(metadata),
                getPlaca(metadata),
                getBatalhao(metadata),
                getMatricula(metadata),
                getUserId(user),
                getUserBatalhaoId(user)
        );

        try {
            List<String> urls = awsS3Service.generatePresignedUrls(request);
            LOGGER.info(
                    "presigned_urls_succeeded clientSubmissionId={} quantidade={} generated={} durationMs={}",
                    clientSubmissionId,
                    quantidade,
                    urls.size(),
                    System.currentTimeMillis() - startedAt
            );
            return ResponseEntity.ok(urls);
        } catch (RuntimeException e) {
            LOGGER.error(
                    "presigned_urls_failed clientSubmissionId={} quantidade={} durationMs={} error={}",
                    clientSubmissionId,
                    quantidade,
                    System.currentTimeMillis() - startedAt,
                    e.getMessage(),
                    e
            );
            throw e;
        }
    }

    private ChecklistUploadMetadata getMetadata(ChecklistUploadRequestDTO request) {
        return request == null ? null : request.getMetadata();
    }

    private String getClientSubmissionId(ChecklistUploadMetadata metadata) {
        return metadata == null ? null : metadata.getClientSubmissionId();
    }

    private String getPrefixo(ChecklistUploadMetadata metadata) {
        return metadata == null ? null : metadata.getPrefixo();
    }

    private String getPlaca(ChecklistUploadMetadata metadata) {
        return metadata == null ? null : metadata.getPlaca();
    }

    private String getBatalhao(ChecklistUploadMetadata metadata) {
        return metadata == null ? null : metadata.getBatalhao();
    }

    private String getMatricula(ChecklistUploadMetadata metadata) {
        return metadata == null ? null : metadata.getMatricula();
    }

    private User getAuthenticatedUser(Authentication authentication) {
        Object principal = authentication == null ? null : authentication.getPrincipal();
        return principal instanceof User ? (User) principal : null;
    }

    private Long getUserId(User user) {
        return user == null ? null : user.getId();
    }

    private Long getUserBatalhaoId(User user) {
        return user == null || user.getBatalhao() == null ? null : user.getBatalhao().getId();
    }
}
