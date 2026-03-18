package pmto._bpm.viaturas.auth.passwordreset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {

    @NotBlank(message = "Token é requerido.")
    private String token;

    @NotBlank(message = "Nova senha é requerida.")
    @Size(min = 6, message = "Nova senha deve ter ao menos 6 caracteres.")
    private String newPassword;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
