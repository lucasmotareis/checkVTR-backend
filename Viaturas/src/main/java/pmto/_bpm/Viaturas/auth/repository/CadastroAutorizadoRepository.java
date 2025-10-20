package pmto._bpm.Viaturas.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import pmto._bpm.Viaturas.auth.model.CadastroAutorizado;

public interface CadastroAutorizadoRepository extends JpaRepository<CadastroAutorizado, Long> {

    Optional<CadastroAutorizadoRepository> findByCpfAndMatricula(String cpf, String matricula);

}
