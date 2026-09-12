package pmto._bpm.viaturas.storage.s3;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pmto._bpm.viaturas.checklists.dto.ChecklistUploadMetadata;
import pmto._bpm.viaturas.checklists.dto.ChecklistUploadRequestDTO;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AwsS3ServiceTest {

    @Test
    void checklistAndProfilePresignUseConfiguredBucketsAndPrefixWithoutCallingAws() throws Exception {
        S3StorageProperties properties = new S3StorageProperties();
        properties.setChecklistBucket("checklists-homolog");
        properties.setProfileBucket("profiles-homolog");
        properties.setKeyPrefix("homolog");

        S3Presigner presigner = mock(S3Presigner.class);
        PresignedPutObjectRequest signed = mock(PresignedPutObjectRequest.class);
        when(signed.url()).thenReturn(new URL("https://example.com/test-upload"));
        when(presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(signed);
        AwsS3Service service = new AwsS3Service(properties, presigner);

        ChecklistUploadMetadata metadata = new ChecklistUploadMetadata();
        metadata.setBatalhao("test-battalion");
        metadata.setPrefixo("test-vehicle");
        metadata.setPlaca("TEST123");
        metadata.setMatricula("test-user");
        ChecklistUploadRequestDTO request = new ChecklistUploadRequestDTO();
        request.setMetadata(metadata);
        request.setQuantidade(1);
        assertEquals(1, service.generatePresignedUrls(request).size());

        ArgumentCaptor<PutObjectPresignRequest> captor = ArgumentCaptor.forClass(PutObjectPresignRequest.class);
        verify(presigner).presignPutObject(captor.capture());
        assertEquals("checklists-homolog", captor.getValue().putObjectRequest().bucket());
        assertTrue(captor.getValue().putObjectRequest().key().startsWith("homolog/checklists/"));

        var upload = service.gerarUploadFotoPerfil(1L);
        assertTrue(upload.fileKey().startsWith("homolog/usuarios/1/perfil/"));
        verify(presigner, org.mockito.Mockito.times(2)).presignPutObject(captor.capture());
        var profileRequest = captor.getValue().putObjectRequest();
        assertEquals("profiles-homolog", profileRequest.bucket());
        assertEquals(upload.fileKey(), profileRequest.key());
    }
}
