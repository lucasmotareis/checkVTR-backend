package pmto._bpm.viaturas.viaturas.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pmto._bpm.viaturas.batalhao.model.Batalhao;
import pmto._bpm.viaturas.users.model.User;
import pmto._bpm.viaturas.viaturas.dto.AtualizarZonaViaturaDTO;
import pmto._bpm.viaturas.viaturas.dto.ViaturaByIdDTO;
import pmto._bpm.viaturas.viaturas.dto.ViaturaComBadgeDTO;
import pmto._bpm.viaturas.viaturas.dto.ViaturaDTO;
import pmto._bpm.viaturas.viaturas.model.Viatura;
import pmto._bpm.viaturas.viaturas.repository.ZonaViaturaCidadeRepository;
import pmto._bpm.viaturas.viaturas.repository.ViaturaRepository;
import pmto._bpm.viaturas.viaturas.repository.ZonaViaturaRepository;

@Service
public class ViaturaService {
    public final ViaturaRepository viaturaRepository;
    public final ZonaViaturaRepository zonaViaturaRepository;
    public final ZonaViaturaCidadeRepository zonaViaturaCidadeRepository;

    public ViaturaService(
            ViaturaRepository viaturaRepository,
            ZonaViaturaRepository zonaViaturaRepository,
            ZonaViaturaCidadeRepository zonaViaturaCidadeRepository
    ) {
        this.viaturaRepository = viaturaRepository;
        this.zonaViaturaRepository = zonaViaturaRepository;
        this.zonaViaturaCidadeRepository = zonaViaturaCidadeRepository;
    }

    public Viatura save(ViaturaDTO dto, User user) {
        if (user == null || user.getBatalhao() == null || user.getBatalhao().getId() == null) {
            throw new AccessDeniedException("Usuario sem batalhao associado.");
        }

        Batalhao batalhao = user.getBatalhao();

        Viatura viatura = new Viatura();
        viatura.setPlaca(dto.getPlaca());
        viatura.setPrefixo(dto.getPrefixo());
        viatura.setModelo(dto.getModelo());
        viatura.setManutencao(dto.isManutencao());
        viatura.setKmAtual(dto.getKmAtual());
        viatura.setKmRevisao(dto.getKmRevisao());
        viatura.setCombustivelAtualPercentual(dto.getCombustivelAtualPercentual());
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
        existente.setCombustivelAtualPercentual(dto.getCombustivelAtualPercentual());

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

    @Transactional
    public void atualizarZona(Long viaturaId, AtualizarZonaViaturaDTO dto, User user) {
        Long batalhaoId = requireBatalhaoId(user);

        Viatura viatura = viaturaRepository.findByIdForUpdate(viaturaId)
                .orElseThrow(() -> new EntityNotFoundException("Viatura nao encontrada com ID: " + viaturaId));

        if (!viatura.getBatalhao().getId().equals(batalhaoId)) {
            throw new AccessDeniedException("Voce nao pode alterar viaturas de outro batalhao.");
        }

        if (dto.getZonaId() == null && dto.getZonaCidadeId() == null) {
            viatura.setZona(null);
            viatura.setZonaCidade(null);
            viaturaRepository.save(viatura);
            return;
        }

        if (dto.getZonaCidadeId() != null) {
            var zonaCidade = zonaViaturaCidadeRepository.findById(dto.getZonaCidadeId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Cidade da zona nao encontrada com ID: " + dto.getZonaCidadeId()
                    ));

            if (!zonaCidade.getZona().getBatalhao().getId().equals(batalhaoId)) {
                throw new AccessDeniedException("Voce nao pode vincular viaturas a cidades de outro batalhao.");
            }

            if (dto.getZonaId() != null && !zonaCidade.getZona().getId().equals(dto.getZonaId())) {
                throw new AccessDeniedException("Cidade da zona nao pertence a zona informada.");
            }

            viatura.setZona(zonaCidade.getZona());
            viatura.setZonaCidade(zonaCidade);
            viaturaRepository.save(viatura);
            return;
        }

        if (dto.getZonaId() == null) {
            throw new AccessDeniedException("Zona invalida para a movimentacao da viatura.");
        }

        var zona = zonaViaturaRepository.findById(dto.getZonaId())
                .orElseThrow(() -> new EntityNotFoundException("Zona nao encontrada com ID: " + dto.getZonaId()));

        if (!zona.getBatalhao().getId().equals(batalhaoId)) {
            throw new AccessDeniedException("Voce nao pode vincular viaturas a zonas de outro batalhao.");
        }

        viatura.setZona(zona);
        viatura.setZonaCidade(null);
        viaturaRepository.save(viatura);
    }

    public ViaturaByIdDTO toByIdDTO(Viatura v) {
        return new ViaturaByIdDTO(
                v.getId(),
                v.getPrefixo(),
                v.getPlaca(),
                v.getModelo(),
                v.getKmAtual(),
                v.getKmRevisao(),
                v.getCombustivelAtualPercentual(),
                v.isManutencao(),
                v.getBatalhao().getId(),
                v.getBatalhao().getNome()
        );
    }

    private Long requireBatalhaoId(User user) {
        if (user == null || user.getBatalhao() == null || user.getBatalhao().getId() == null) {
            throw new AccessDeniedException("Usuario sem batalhao associado.");
        }

        return user.getBatalhao().getId();
    }
}
