package pmto._bpm.viaturas.model;


import jakarta.persistence.*;

@Entity
@Table(name="batalhao")
public class Batalhao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    public Batalhao() {
    }

    public Long getId() {
        return id;
    }

    public Batalhao(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
