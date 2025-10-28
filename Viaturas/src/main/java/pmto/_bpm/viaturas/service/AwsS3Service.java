package pmto._bpm.viaturas.service;

import org.springframework.stereotype.Service;
import pmto._bpm.viaturas.dto.ChecklistUploadMetadata;
import pmto._bpm.viaturas.dto.ChecklistUploadRequestDTO;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
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

    public List<String> generatePresignedUrls(ChecklistUploadRequestDTO request) {


        Region region = Region.of("us-east-2"); // Substitua pela sua região

        S3Presigner presigner = S3Presigner.builder()
                .region(region)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create()) // ou use IAM role, etc.
                .build();

        List<String> urls = new ArrayList<>();
        String dataHoje = LocalDate.now().toString(); // YYYY-MM-DD
        ChecklistUploadMetadata meta = request.getMetadata();


        for (int i = 0; i < request.getQuantidade(); i++) {
            String fileName = UUID.randomUUID().toString() + ".jpg";

            String key = String.format("checklists/%s/%s/%s/%s/%s/%s",
                    dataHoje,
                    meta.getBatalhao(),
                    meta.getPrefixo(),
                    meta.getPlaca(),
                    meta.getMatricula(),
                    fileName);

            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket("fotos-viatura-pmto")
                    .key(key)
                    .contentType("image/jpeg")
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(1))
                    .putObjectRequest(objectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);

            urls.add(presignedRequest.url().toString());
        }
        return urls;

    }
}