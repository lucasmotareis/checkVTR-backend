package pmto._bpm.viaturas.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfigurationSource;
import pmto._bpm.viaturas.storage.s3.S3StorageProperties;

import static org.assertj.core.api.Assertions.assertThat;

class HomologConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            // Testar os defaults do perfil, sem herdar overrides de Maven/CI ou do host.
            .withInitializer(context -> {
                var sources = context.getEnvironment().getPropertySources();
                sources.remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
                sources.remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
            })
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
            .withUserConfiguration(PropertiesConfig.class)
            .withPropertyValues("spring.profiles.active=homolog", "POSTGRES_PASSWORD=test-only-password",
                    "JWT_SECRET=test-only-homolog-secret-at-least-32-bytes");

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(S3StorageProperties.class)
    @Import(CorsConfig.class)
    static class PropertiesConfig {
    }

    @Test
    void homologProfileLoadsIsolatedDefaultsAndRealCorsBinding() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            var properties = context.getBean(S3StorageProperties.class);
            assertThat(properties.getChecklistBucket()).isEqualTo("homolog-fotos-viaturas");
            assertThat(properties.getProfileBucket()).isEqualTo("homolog-fotos-viaturas");
            assertThat(properties.getKeyPrefix()).isEqualTo("homolog");
            assertThat(properties.isIsolated()).isTrue();
            assertThat(context.getEnvironment().getProperty("spring.datasource.url"))
                    .isEqualTo("jdbc:postgresql://db:5432/frotapm_homolog");
            var cors = context.getBean(CorsConfigurationSource.class)
                    .getCorsConfiguration(new MockHttpServletRequest("OPTIONS", "/auth/login"));
            assertThat(cors.checkOrigin("https://homolog_app.pmto8bpm.com.br"))
                    .isEqualTo("https://homolog_app.pmto8bpm.com.br");
            assertThat(cors.checkOrigin("https://web.pmto8bpm.com.br")).isNull();
        });
    }

    @Test
    void emptyPrefixFailsStartupInHomolog() {
        runner.withPropertyValues("S3_KEY_PREFIX=").run(context -> assertThat(context).hasFailed());
    }
}
