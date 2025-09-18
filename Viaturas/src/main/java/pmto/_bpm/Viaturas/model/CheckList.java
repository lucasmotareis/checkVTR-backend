package pmto._bpm.Viaturas.model;
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

    private Integer KmAtual;

    @OneToMany(mappedBy = "checklist", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ItemCheckList> itens = new ArrayList<>();


    public CheckList() {
        this.data = LocalDateTime.now();
    }

    public CheckList(List<ItemCheckList> itens, Integer kmAtual, List<String> imagens, Viatura viatura, Long id, LocalDateTime data) {
        this.itens = itens;
        this.KmAtual = kmAtual;
        this.imagens = imagens;
        this.viatura = viatura;
        this.id = id;
        this.data = data;
    }

    public Integer getKmAtual() {
        return KmAtual;
    }

    public void setKmAtual(Integer kmAtual) {
        KmAtual = kmAtual;
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

    public List<ItemCheckList> getItens() {
        return itens;
    }

    public void setItens(List<ItemCheckList> itens) {
        this.itens = itens;
    }
}
