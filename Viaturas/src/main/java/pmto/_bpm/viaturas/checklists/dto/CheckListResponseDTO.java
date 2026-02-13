package pmto._bpm.viaturas.checklists.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class CheckListResponseDTO {
    private Long id;
    private Integer kmAtual;
    private Integer kmRevisao;
    private String nomeGuerra;
    private String matricula;
    private String prefixo;
    private Instant data;

    private List<String> imagens;
    private List<CheckListProblemaDTO> problemas;

}

