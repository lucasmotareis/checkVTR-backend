package pmto._bpm.Viaturas.auth.repository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pmto._bpm.Viaturas.auth.model.User;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long>{
    Optional<User> findByCPF(String cpf);

    @EntityGraph(attributePaths = "batalhao")
    Optional<User> findByMatricula(String matricula);

    boolean existsByMatricula(String matricula);
    boolean existsByCPF(String cpf);

}
