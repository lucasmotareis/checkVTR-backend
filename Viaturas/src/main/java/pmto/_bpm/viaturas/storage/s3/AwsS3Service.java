package pmto._bpm.viaturas.storage.s3;

import org.springframework.stereotype.Service;
import pmto._bpm.viaturas.checklists.dto.ChecklistUploadMetadata;
import pmto._bpm.viaturas.checklists.dto.ChecklistUploadRequestDTO;
import pmto._bpm.viaturas.storage.dto.PresignedUpload;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
public class AwsS3Service {

    private final S3Presigner presigner;
    private final S3StorageProperties storageProperties;

    public AwsS3Service(S3StorageProperties storageProperties, S3Presigner presigner) {
        this.storageProperties = storageProperties;
        this.presigner = presigner;
    }

    public List<String> generatePresignedUrls(ChecklistUploadRequestDTO request) {

        List<String> urls = new ArrayList<>();
        String dataHoje = LocalDate.now().toString(); // YYYY-MM-DD
        ChecklistUploadMetadata meta = request.getMetadata();


        for (int i = 0; i < request.getQuantidade(); i++) {
            String fileName = UUID.randomUUID() + ".jpg";

            String key = String.format("checklists/%s/%s/%s/%s/%s/%s",
                    dataHoje,
                    meta.getBatalhao(),
                    meta.getPrefixo(),
                    meta.getPlaca(),
                    meta.getMatricula(),
                    fileName);

            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(storageProperties.getChecklistBucket())
                    .key(storageProperties.prefixedKey(key))
                    .contentType("image/jpeg")
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .putObjectRequest(objectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);

            urls.add(presignedRequest.url().toString());
        }
        return urls;

    }

     public PresignedUpload gerarUploadFotoPerfil(Long userId) {

        String key = storageProperties.prefixedKey(String.format(
                "usuarios/%d/perfil/%s",
                userId,
                UUID.randomUUID()
        ));

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(storageProperties.getProfileBucket())
                .key(key)
                .contentType("image/jpeg")
                .build();

        PresignedPutObjectRequest presignedRequest =
                presigner.presignPutObject(
                        PutObjectPresignRequest.builder()
                                .signatureDuration(Duration.ofMinutes(10))
                                .putObjectRequest(objectRequest)
                                .build()
                );

        return new PresignedUpload(
                presignedRequest.url().toString(),
                key
        );
    }


}
