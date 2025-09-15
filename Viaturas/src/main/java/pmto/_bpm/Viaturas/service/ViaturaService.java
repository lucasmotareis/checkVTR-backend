package pmto._bpm.Viaturas.service;

import org.springframework.stereotype.Service;
import pmto._bpm.Viaturas.model.Viatura;
import pmto._bpm.Viaturas.repository.ViaturaRepository;

import java.util.List;


@Service
public class ViaturaService {
    public final ViaturaRepository viaturaRepository;

    public ViaturaService(ViaturaRepository viaturaRepository) {
        this.viaturaRepository = viaturaRepository;
    }

    public List<Viatura> getAll() {
        return viaturaRepository.findAll();
    }

    public Viatura save(Viatura viatura){
        return viaturaRepository.save(viatura);
    }

    public void delete(Long id_viatura){
        viaturaRepository.deleteById(id_viatura);
    }



}
