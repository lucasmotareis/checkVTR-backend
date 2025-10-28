package pmto._bpm.viaturas.auth.dto;

import jakarta.validation.constraints.NotBlank;


public class LoginRequest {

    @NotBlank
    private String matricula;

    @NotBlank
    private String senha;

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
