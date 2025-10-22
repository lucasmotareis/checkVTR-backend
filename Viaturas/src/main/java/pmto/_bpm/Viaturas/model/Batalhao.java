package pmto._bpm.Viaturas.model;


import jakarta.persistence.*;

import java.util.List;

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
