package pmto._bpm.viaturas.auth.emailverification.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "auth.email-verification")
public class EmailVerificationMailProperties {

    @NotBlank(message = "auth.email-verification.verify-url-base must be configured.")
    private String verifyUrlBase;

    @NotBlank(message = "auth.email-verification.from-email must be configured.")
    @Email(message = "auth.email-verification.from-email must be a valid email.")
    private String fromEmail;

    public String getVerifyUrlBase() {
        return verifyUrlBase;
    }

    public void setVerifyUrlBase(String verifyUrlBase) {
        this.verifyUrlBase = verifyUrlBase;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }
}
