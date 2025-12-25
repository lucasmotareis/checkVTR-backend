package pmto._bpm.viaturas.service;

import org.springframework.stereotype.Service;
import pmto._bpm.viaturas.auth.model.User;
import pmto._bpm.viaturas.auth.repository.UserRepository;
import pmto._bpm.viaturas.dto.ChecklistUploadMetadata;
import pmto._bpm.viaturas.dto.ChecklistUploadRequestDTO;
import pmto._bpm.viaturas.dto.PresignedDownload;
import pmto._bpm.viaturas.dto.PresignedUpload;
import pmto._bpm.viaturas.exception.RegisterException;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
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
    private final Region region = Region.of("sa-east-1");
    private final UserRepository userRepository;

    public AwsS3Service(UserRepository userRepository) {
        this.presigner = S3Presigner.builder()
                .region(region)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();
        this.userRepository = userRepository;

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
                    .bucket("fotos-viaturas")
                    .key(key)
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

    public PresignedDownload gerarDownloadCnh(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RegisterException("Usuário não encontrado"));

        if (user.getCnhKey() == null) {
            throw new RegisterException("CNH não enviada");
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket("motoristas-documentos")
                .key(user.getCnhKey())
                .build();

        PresignedGetObjectRequest presignedRequest =
                presigner.presignGetObject(
                        GetObjectPresignRequest.builder()
                                .signatureDuration(Duration.ofMinutes(2))
                                .getObjectRequest(getObjectRequest)
                                .build()
                );

        return new PresignedDownload(
                presignedRequest.url().toString()
        );
    }

     public PresignedUpload gerarUploadFotoPerfil(Long userId) {

        String key = String.format(
                "usuarios/%d/perfil/%s",
                userId,
                UUID.randomUUID()
        );

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket("usuarios-perfil")
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

    public PresignedUpload gerarUploadCnh(Long userId) {

        String key = String.format(
                "usuarios/%d/cnh/%s",
                userId,
                UUID.randomUUID()
        );

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket("motoristas-documentos")
                .key(key)
                .contentType("application/pdf")
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