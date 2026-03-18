package pmto._bpm.viaturas.auth.passwordreset.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "auth.password-reset")
public class PasswordResetMailProperties {

    @NotBlank(message = "auth.password-reset.reset-url-base must be configured.")
    private String resetUrlBase;

    @NotBlank(message = "auth.password-reset.from-email must be configured.")
    @Email(message = "auth.password-reset.from-email must be a valid email.")
    private String fromEmail;

    public String getResetUrlBase() {
        return resetUrlBase;
    }

    public void setResetUrlBase(String resetUrlBase) {
        this.resetUrlBase = resetUrlBase;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }
}
