package pmto._bpm.viaturas.auth.emailverification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ResendVerificationEmailRequest {

    @NotBlank(message = "Email e necessario.")
    @Email(message = "Email deve ser valido.")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
