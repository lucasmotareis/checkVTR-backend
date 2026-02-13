package pmto._bpm.viaturas.notifications.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import pmto._bpm.viaturas.batalhao.model.Batalhao;

import java.time.Instant;
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

    private String mensagem;

    @ManyToOne
    @JoinColumn(name = "batalhao_id")
    private Batalhao batalhao;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private Instant dataCriacao;

    // Construtor vazio
    public Notification() {

    }

    // Construtor completo (sem data, que será gerada automaticamente)
    public Notification(Batalhao batalhao, String titulo, String mensagem) {
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.batalhao = batalhao;
    }


}
