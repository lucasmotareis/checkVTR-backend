package pmto._bpm.viaturas.auth.dto;

public class AuthResponse {


    private String token;
    UserResponse user;

    public AuthResponse(String token, UserResponse user) {
        this.token = token;
        this.user =  user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }
}
