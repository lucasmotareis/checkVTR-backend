package pmto._bpm.viaturas.controller;

import pmto._bpm.viaturas.dto.CheckListProblemaDTO;

import java.util.List;

public class CheckListResponseDTO {
    private Long id;
    private Integer kmAtual;
    private String nomeUsuario;
    private List<String> imagens;
    private List<CheckListProblemaDTO> problemas;


    public Integer getKmAtual() {
        return kmAtual;
    }

    public void setKmAtual(Integer kmAtual) {
        this.kmAtual = kmAtual;
    }


    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public List<CheckListProblemaDTO> getProblemas() {
        return problemas;
    }

    public void setProblemas(List<CheckListProblemaDTO> problemas) {
        this.problemas = problemas;
    }

    public List<String> getImagens() {
        return imagens;
    }

    public void setImagens(List<String> imagens) {
        this.imagens = imagens;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

