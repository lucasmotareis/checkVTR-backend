package pmto._bpm.viaturas.auth.model;
import jakarta.persistence.*;


@Entity
@Table(name = "cadastro_autorizado")
public class CadastroAutorizado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cpf;

    private String matricula;

    public CadastroAutorizado() {}

    public CadastroAutorizado(String cpf, String matricula) {
        this.cpf = cpf;
        this.matricula = matricula;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
}
