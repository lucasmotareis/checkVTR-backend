package pmto._bpm.viaturas.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;


public class RegisterRequest {

    @NotBlank
    private String nomeGuerra;

    @NotBlank
    private String graduacao;

    @NotBlank
    private String senha;

    @NotBlank
    private String cpf;

    @NotBlank
    private String matricula;

    @Email
    private String email;

    @NotNull
    private Long batalhaoId;

    public String getGraduacao() {
        return graduacao;
    }

    public void setGraduacao(String graduacao) {
        this.graduacao = graduacao;
    }

    public Long getBatalhaoId() {
        return batalhaoId;
    }

    public void setBatalhaoId(Long batalhaoId) {
        this.batalhaoId = batalhaoId;
    }

    public String getNomeGuerra() {
        return nomeGuerra;
    }

    public void setNomeGuerra(String nomeGuerra) {
        this.nomeGuerra = nomeGuerra;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
