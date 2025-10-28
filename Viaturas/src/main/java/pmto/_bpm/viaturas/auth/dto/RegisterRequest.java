package pmto._bpm.viaturas.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public class RegisterRequest {

    @NotBlank
    private String nome_guerra;

    @NotBlank
    private String senha;

    @NotBlank
    private String cpf;

    @NotBlank
    private String matricula;


    @NotNull
    private Long batalhaoId;

    public Long getBatalhaoId() {
        return batalhaoId;
    }

    public void setBatalhaoId(Long batalhaoId) {
        this.batalhaoId = batalhaoId;
    }

    public String getNome_guerra() {
        return nome_guerra;
    }

    public void setNome_guerra(String nome_guerra) {
        this.nome_guerra = nome_guerra;
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
}
