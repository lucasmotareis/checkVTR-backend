package pmto._bpm.viaturas.storage.s3;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "storage.s3")
@Validated
@Getter
@Setter
public class S3StorageProperties {

    @NotBlank
    private String region = "sa-east-1";

    @NotBlank
    private String checklistBucket = "fotos-viaturas";

    @NotBlank
    private String profileBucket = "usuarios-perfil";

    @Pattern(regexp = "(?:[a-zA-Z0-9_-]+/)*[a-zA-Z0-9_-]*")
    private String keyPrefix = "";

    private boolean isolated;

    @AssertTrue(message = "An isolated storage environment requires a non-empty key prefix")
    public boolean isIsolationValid() {
        return !isolated || (keyPrefix != null && !keyPrefix.isBlank());
    }

    public String prefixedKey(String key) {
        return keyPrefix.isEmpty() ? key : keyPrefix + "/" + key;
    }

    public String profilePublicUrl(String key) {
        if (!keyPrefix.isEmpty() && !key.startsWith(keyPrefix + "/")) {
            throw new IllegalArgumentException("Profile upload key belongs to another storage environment");
        }
        return "https://" + profileBucket + ".s3." + region + ".amazonaws.com/" + key;
    }
}
