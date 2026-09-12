package pmto._bpm.viaturas.storage.s3;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class S3StoragePropertiesTest {

    @Test
    void preservesExistingKeysWhenNoPrefixIsConfigured() {
        S3StorageProperties properties = new S3StorageProperties();
        assertEquals("checklists/photo.jpg", properties.prefixedKey("checklists/photo.jpg"));
        assertEquals("https://usuarios-perfil.s3.sa-east-1.amazonaws.com/usuarios/1/perfil/photo",
                properties.profilePublicUrl("usuarios/1/perfil/photo"));
    }

    @Test
    void isolatesKeysAndBuildsPublicUrlUsingConfiguredBucketAndRegion() {
        S3StorageProperties properties = new S3StorageProperties();
        properties.setKeyPrefix("homolog");
        properties.setProfileBucket("profiles-homolog");
        properties.setRegion("us-east-1");
        assertEquals("homolog/checklists/photo.jpg", properties.prefixedKey("checklists/photo.jpg"));
        assertEquals("https://profiles-homolog.s3.us-east-1.amazonaws.com/homolog/usuarios/1/perfil/photo",
                properties.profilePublicUrl("homolog/usuarios/1/perfil/photo"));
        assertThrows(IllegalArgumentException.class, () -> properties.profilePublicUrl("usuarios/1/perfil/photo"));
    }

    @Test
    void isolatedEnvironmentCannotUseAnEmptyPrefixOrTraversal() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            S3StorageProperties properties = new S3StorageProperties();
            properties.setIsolated(true);
            assertFalse(validator.validate(properties).isEmpty());
            properties.setKeyPrefix("../production");
            assertFalse(validator.validate(properties).isEmpty());
            properties.setKeyPrefix("homolog");
            assertTrue(validator.validate(properties).isEmpty());
        }
    }
}
