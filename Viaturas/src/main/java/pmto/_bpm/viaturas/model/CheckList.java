package pmto._bpm.viaturas.model;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pmto._bpm.viaturas.auth.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "checklist")
@Getter
@Setter
public class CheckList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime data;


    @ManyToOne
    @JoinColumn(name = "viatura_id")
    @JsonBackReference
    private Viatura viatura;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    @JsonBackReference
    private User usuario;

    @ElementCollection
    @CollectionTable(name = "checklist_images", joinColumns = @JoinColumn(name = "checklist_id"))
    @Column(name = "image_url")
    private List<String> imagens = new ArrayList<>();

    private Integer kmAtual;

    private Integer kmRevisao;

    @OneToMany(mappedBy = "checklist", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<CheckListProblema> problemas = new ArrayList<>();


    public CheckList() {
        this.data = LocalDateTime.now();
    }

    public CheckList(Integer kmRevisao, User usuario, List<CheckListProblema> problemas, Integer kmAtual, List<String> imagens, Viatura viatura, Long id, LocalDateTime data) {
        this.kmRevisao = kmRevisao;
        this.usuario = usuario;
        this.problemas = problemas;
        this.kmAtual = kmAtual;
        this.imagens = imagens;
        this.viatura = viatura;
        this.id = id;
        this.data = data;
    }
}