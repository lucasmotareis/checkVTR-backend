package pmto._bpm.viaturas.viaturas.service;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import pmto._bpm.viaturas.viaturas.dto.ViaturaByIdDTO;
import pmto._bpm.viaturas.viaturas.dto.ViaturaComBadgeDTO;
import pmto._bpm.viaturas.viaturas.dto.ViaturaDTO;

import org.springframework.stereotype.Service;
import pmto._bpm.viaturas.batalhao.model.Batalhao;
import pmto._bpm.viaturas.viaturas.model.Viatura;
import pmto._bpm.viaturas.batalhao.repository.BatalhaoRepository;
import pmto._bpm.viaturas.viaturas.repository.ViaturaRepository;
import jakarta.persistence.EntityNotFoundException;

import java.time.Instant;
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
        viatura.setKmAtual(dto.getKmAtual());
        viatura.setKmRevisao(dto.getKmRevisao());
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
        existente.setKmAtual(dto.getKmAtual());
        existente.setManutencao(dto.isManutencao());
        existente.setModelo(dto.getModelo());
        existente.setKmRevisao(dto.getKmRevisao());

        return viaturaRepository.save(existente);
    }

    public List<Viatura> listarViaturas(Long batalhaoId) {
        return viaturaRepository.findByBatalhaoId(batalhaoId);
    }

    public List<ViaturaByIdDTO> listarViaturasDTO(Long batalhaoId) {
        return viaturaRepository.findByBatalhaoId(batalhaoId)
                .stream()
                .map(this::toByIdDTO)
                .toList();
    }


    public List<ViaturaComBadgeDTO> listarComBadges(Long batalhaoId) {
        return viaturaRepository.findViaturasComBadge(batalhaoId);
    }

    public List<Viatura> getByBatalhao(Long batalhaoId) {
        return viaturaRepository.findByBatalhaoId(batalhaoId);
    }

    public Viatura getViaturaById(Long id) {
       return viaturaRepository.getById(id);
    }

    public ViaturaByIdDTO getViaturaById2(Long id) {
        Viatura v = viaturaRepository.findById(id)
                .orElseThrow();

        return toByIdDTO(v);
    }

    public ViaturaByIdDTO toByIdDTO(Viatura v) {
        return new ViaturaByIdDTO(
                v.getId(),
                v.getPrefixo(),
                v.getPlaca(),
                v.getModelo(),
                v.getKmAtual(),
                v.getKmRevisao(),
                v.isManutencao(),
                v.getBatalhao().getId(),
                v.getBatalhao().getNome()
        );
    }

}
