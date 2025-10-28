package pmto._bpm.viaturas.model;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "checklist")
public class CheckList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime data;


    @ManyToOne
    @JoinColumn(name = "viatura_id")
    @JsonBackReference
    private Viatura viatura;

    @ElementCollection
    @CollectionTable(name = "checklist_images", joinColumns = @JoinColumn(name = "checklist_id"))
    @Column(name = "image_url")
    private List<String> imagens = new ArrayList<>();

    private Integer kmAtual;

    @OneToMany(mappedBy = "checklist", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<CheckListProblema> problemas = new ArrayList<>();



    public CheckList() {
        this.data = LocalDateTime.now();
    }

    public CheckList(List<CheckListProblema> problemas, Integer kmAtual, List<String> imagens, Viatura viatura, Long id, LocalDateTime data) {
        this.problemas = problemas;
        this.kmAtual = kmAtual;
        this.imagens = imagens;
        this.viatura = viatura;
        this.id = id;
        this.data = data;
    }

    public Integer getKmAtual() {
        return kmAtual;
    }

    public void setKmAtual(Integer kmAtual) {
        this.kmAtual = kmAtual;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Viatura getViatura() {
        return viatura;
    }

    public void setViatura(Viatura viatura) {
        this.viatura = viatura;
    }



    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public List<String> getImagens() {
        return imagens;
    }

    public void setImagens(List<String> imagens) {
        this.imagens = imagens;
    }

    public List<CheckListProblema> getProblemas() {
        return problemas;
    }



    public void setProblemas(List<CheckListProblema> problemas) {
        this.problemas = problemas;
    }
}
