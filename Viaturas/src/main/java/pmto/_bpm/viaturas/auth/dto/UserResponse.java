package pmto._bpm.viaturas.auth.dto;

public class UserResponse {
    private String nome_guerra;
    private String batalhao;
    private String matricula;
    private String role;

    public UserResponse(String nome_guerra, String batalhao, String role, String matricula) {
        this.nome_guerra = nome_guerra;
        this.batalhao = batalhao;
        this.role = role;
        this.matricula = matricula;
    }

    public String getNome_guerra() {
        return nome_guerra;
    }

    public void setNome_guerra(String nome_guerra) {
        this.nome_guerra = nome_guerra;
    }

    public String getBatalhao() {
        return batalhao;
    }

    public void setBatalhao(String batalhao) {
        this.batalhao = batalhao;
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
