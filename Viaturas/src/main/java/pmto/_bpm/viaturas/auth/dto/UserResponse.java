package pmto._bpm.viaturas.auth.dto;

import pmto._bpm.viaturas.auth.model.User;

public class UserResponse {
    private String nomeGuerra;
    private String batalhao;
    private String matricula;
    private String role;
    private Long idBatalhao;

    public UserResponse(User user) {
        this.idBatalhao = user.getBatalhao().getId();
        this.nomeGuerra = user.getNome_guerra();
        this.batalhao = user.getBatalhao().getNome();
        this.role = user.getRole().toString();
        this.matricula = user.getMatricula();
    }

    public String getNomeGuerra() {
        return nomeGuerra;
    }

    public void setNomeGuerra(String nomeGuerra) {
        this.nomeGuerra = nomeGuerra;
    }

    public Long getIdBatalhao() {
        return idBatalhao;
    }

    public void setIdBatalhao(Long idBatalhao) {
        this.idBatalhao = idBatalhao;
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
