package pmto._bpm.viaturas.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CheckListResponseDTO {
    private Long id;
    private Integer kmAtual;
    private String nomeGuerra;
    private String matricula;
    private LocalDateTime data;

    private List<String> imagens;
    private List<CheckListProblemaDTO> problemas;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeGuerra() {
        return nomeGuerra;
    }

    public void setNomeGuerra(String nomeGuerra) {
        this.nomeGuerra = nomeGuerra;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public Integer getKmAtual() {
        return kmAtual;
    }

    public void setKmAtual(Integer kmAtual) {
        this.kmAtual = kmAtual;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public List<String> getImagens() {
        return imagens;
    }

    public void setImagens(List<String> imagens) {
        this.imagens = imagens;
    }

    public List<CheckListProblemaDTO> getProblemas() {
        return problemas;
    }

    public void setProblemas(List<CheckListProblemaDTO> problemas) {
        this.problemas = problemas;
    }
}

