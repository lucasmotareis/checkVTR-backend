package pmto._bpm.viaturas.service;
import org.springframework.beans.factory.annotation.Autowired;
import pmto._bpm.viaturas.dto.ViaturaDTO;

import org.springframework.stereotype.Service;
import pmto._bpm.viaturas.model.Batalhao;
import pmto._bpm.viaturas.model.Viatura;
import pmto._bpm.viaturas.repository.BatalhaoRepository;
import pmto._bpm.viaturas.repository.ViaturaRepository;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;


@Service
public class ViaturaService {
    @Autowired
    private BatalhaoRepository batalhaoRepository;

    public final ViaturaRepository viaturaRepository;

    public ViaturaService(ViaturaRepository viaturaRepository) {
        this.viaturaRepository = viaturaRepository;
    }


    public Viatura save(ViaturaDTO dto) {

        Batalhao batalhao = batalhaoRepository.findById(dto.getBatalhaoId())
                .orElseThrow(() -> new IllegalArgumentException("Batalhão não encontrado"));

        Viatura viatura = new Viatura();
        viatura.setPlaca(dto.getPlaca());
        viatura.setPrefixo(dto.getPrefixo());
        viatura.setModelo(dto.getModelo());
        viatura.setManutencao(dto.isManutencao());
        viatura.setKm_atual(dto.getKmAtual());
        viatura.setKm_revisao(dto.getKmRevisao());
        viatura.setBatalhao(batalhao);
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
        existente.setManutencao(dto.isManutencao());
        existente.setModelo(dto.getModelo());
        existente.setKm_revisao(dto.getKmRevisao());

        return viaturaRepository.save(existente);
    }

    public List<Viatura> getByBatalhao(Long batalhaoId) {
        return viaturaRepository.findByBatalhaoId(batalhaoId);
    }

    public Viatura getViaturaById(Long id) {
        return viaturaRepository.getById(id);
    }

}
