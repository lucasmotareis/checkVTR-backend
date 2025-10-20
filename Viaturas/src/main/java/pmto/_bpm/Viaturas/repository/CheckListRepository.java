package pmto._bpm.Viaturas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pmto._bpm.Viaturas.model.CheckList;

import java.util.List;

public interface CheckListRepository extends JpaRepository<CheckList, Long> {
    List<CheckList> findByViaturaId(Long id);
}
