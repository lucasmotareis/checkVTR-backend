package pmto._bpm.viaturas.auth.passwordreset.dto;

public class PasswordResetResponse {

    private final String message;

    public PasswordResetResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
