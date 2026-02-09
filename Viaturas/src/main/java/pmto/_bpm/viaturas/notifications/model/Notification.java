package pmto._bpm.viaturas.notifications.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import pmto._bpm.viaturas.batalhao.model.Batalhao;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    private String descricao;

    @ManyToOne
    @JoinColumn(name = "batalhao_id")
    private Batalhao batalhao;

    @CreationTimestamp
    private LocalDateTime dataCriacao;

    // Construtor vazio
    public Notification() {
        this.dataCriacao = LocalDateTime.now();
    }

    // Construtor completo (sem data, que será gerada automaticamente)
    public Notification(Batalhao batalhao, String titulo, String descricao) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.dataCriacao = LocalDateTime.now();
        this.batalhao = batalhao;
    }


}
