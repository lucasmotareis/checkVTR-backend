package pmto._bpm.Viaturas.service;
import org.springframework.web.bind.annotation.PathVariable;
import pmto._bpm.Viaturas.dto.ViaturaDTO;

import org.springframework.stereotype.Service;
import pmto._bpm.Viaturas.model.Viatura;
import pmto._bpm.Viaturas.repository.ViaturaRepository;
import jakarta.persistence.EntityNotFoundException;

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

    public Viatura getViaturaById(@PathVariable Long id){
        return viaturaRepository.findById(id).get();
    }

    public Viatura save(ViaturaDTO dto) {
        Viatura viatura = new Viatura();
        viatura.setPlaca(dto.getPlaca());
        viatura.setPrefixo(dto.getPrefixo());
        viatura.setModelo(dto.getModelo());
        viatura.setKm_atual(dto.getKmAtual());
        viatura.setKm_revisao(dto.getKmRevisao());
        return viaturaRepository.save(viatura);
    }


    public void delete(Long id) {
        if (!viaturaRepository.existsById(id)) {
            throw new EntityNotFoundException("Viatura não encontrada com ID " + id);
        }
        viaturaRepository.deleteById(id);
    }


    public Viatura atualizar(Long id, ViaturaDTO dto) {
        Viatura existente = viaturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Viatura não encontrada com ID: " + id));

        existente.setPlaca(dto.getPlaca());
        existente.setPrefixo(dto.getPrefixo());
        existente.setKm_atual(dto.getKmAtual());
        existente.setModelo(dto.getModelo());
        existente.setKm_revisao(dto.getKmRevisao());

        return viaturaRepository.save(existente);
    }


}
