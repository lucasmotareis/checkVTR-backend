package pmto._bpm.Viaturas.auth.dto;

import pmto._bpm.Viaturas.auth.model.User;

public class AuthResponse {


    private String token;
    User user;

    public AuthResponse(String token, User user) {
        this.token = token;
        this.user =  user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
