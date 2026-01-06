package pmto._bpm.viaturas.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="batalhao")
@Getter
@Setter
public class Batalhao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    public Batalhao() {
    }


}
