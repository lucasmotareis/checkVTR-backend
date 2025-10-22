package pmto._bpm.Viaturas.auth.dto;

public class AuthResponse {


    private String token;
    private String nome_guerra;
    private String matricula;
    private String batalhao;
    private String role;

    public AuthResponse(String token, String nome_guerra,String batalhao, String matricula, String role) {
        this.token = token;
        this.nome_guerra = nome_guerra;
        this.batalhao = batalhao;
        this.matricula = matricula;
        this.role = role;

    }

    public String getBatalhao() {
        return batalhao;
    }

    public void setBatalhao(String batalhao) {
        this.batalhao = batalhao;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNome_guerra() {
        return nome_guerra;
    }

    public void setNome_guerra(String nome_guerra) {
        this.nome_guerra = nome_guerra;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
