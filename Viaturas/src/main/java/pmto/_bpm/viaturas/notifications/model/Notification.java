package pmto._bpm.viaturas.notifications.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import pmto._bpm.viaturas.batalhao.model.Batalhao;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String titulo;

    @Column(nullable = false, length = 500)
    private String mensagem;

    @ManyToOne
    @JoinColumn(name = "batalhao_id")
    private Batalhao batalhao;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private Instant dataCriacao;

    public Notification() {
    }

    public Notification(Batalhao batalhao, String titulo, String mensagem) {
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.batalhao = batalhao;
    }
}
