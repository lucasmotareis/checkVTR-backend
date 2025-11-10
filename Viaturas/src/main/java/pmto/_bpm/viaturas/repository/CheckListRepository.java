package pmto._bpm.viaturas.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pmto._bpm.viaturas.model.CheckList;

import java.util.List;

public interface CheckListRepository extends JpaRepository<CheckList, Long> {
    Page<CheckList> findByViaturaId(Long viaturaId, Pageable pageable);
}
