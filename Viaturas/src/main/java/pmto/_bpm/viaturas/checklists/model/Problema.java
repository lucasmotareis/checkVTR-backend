package pmto._bpm.viaturas.checklists.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pmto._bpm.viaturas.checklists.dto.CategoriaProblema;

@Entity
@Table(name = "problema")
@Getter
@Setter
public class Problema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = CategoriaProblemaConverter.class)
    @Column(nullable = false)
    private CategoriaProblema categoria;

    @Column(nullable = false)
    private String descricao;

    public boolean deveMonitorarNaViatura() {
        return categoria != null && categoria.isMonitorarNaViatura();
    }

    public int getOrdemCategoria() {
        return categoria != null ? categoria.getOrdemExibicao() : 999;
    }


}
