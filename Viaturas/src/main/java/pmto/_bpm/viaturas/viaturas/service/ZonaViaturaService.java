package pmto._bpm.viaturas.viaturas.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pmto._bpm.viaturas.batalhao.model.Batalhao;
import pmto._bpm.viaturas.users.model.User;
import pmto._bpm.viaturas.viaturas.dto.ReordenarZonaViaturaDTO;
import pmto._bpm.viaturas.viaturas.dto.ViaturaQuadroItemDTO;
import pmto._bpm.viaturas.viaturas.dto.ZonaViaturaCidadeDTO;
import pmto._bpm.viaturas.viaturas.dto.ZonaViaturaCidadeQuadroDTO;
import pmto._bpm.viaturas.viaturas.dto.ZonaViaturaCidadeResponseDTO;
import pmto._bpm.viaturas.viaturas.dto.ZonaViaturaDTO;
import pmto._bpm.viaturas.viaturas.dto.ZonaViaturaQuadroDTO;
import pmto._bpm.viaturas.viaturas.dto.ZonaViaturaResponseDTO;
import pmto._bpm.viaturas.viaturas.model.ZonaViatura;
import pmto._bpm.viaturas.viaturas.model.ZonaViaturaCidade;
import pmto._bpm.viaturas.viaturas.repository.ViaturaRepository;
import pmto._bpm.viaturas.viaturas.repository.ZonaViaturaCidadeRepository;
import pmto._bpm.viaturas.viaturas.repository.ZonaViaturaRepository;

@Service
public class ZonaViaturaService {

    private final ZonaViaturaRepository zonaViaturaRepository;
    private final ZonaViaturaCidadeRepository zonaViaturaCidadeRepository;
    private final ViaturaRepository viaturaRepository;

    public ZonaViaturaService(
            ZonaViaturaRepository zonaViaturaRepository,
            ZonaViaturaCidadeRepository zonaViaturaCidadeRepository,
            ViaturaRepository viaturaRepository
    ) {
        this.zonaViaturaRepository = zonaViaturaRepository;
        this.zonaViaturaCidadeRepository = zonaViaturaCidadeRepository;
        this.viaturaRepository = viaturaRepository;
    }

    @Transactional(readOnly = true)
    public List<ZonaViaturaQuadroDTO> listarQuadro(User user) {
        Long batalhaoId = requireBatalhaoId(user);

        List<ZonaViatura> zonas = zonaViaturaRepository.findByBatalhaoIdOrderByOrdemAsc(batalhaoId);
        List<ZonaViaturaCidade> cidades = zonaViaturaCidadeRepository.findByBatalhaoIdOrderByZonaOrdemAscOrdemAsc(
                batalhaoId
        );
        List<ViaturaQuadroItemDTO> viaturas = viaturaRepository.findViaturasParaQuadro(batalhaoId);

        Map<Long, List<ZonaViaturaCidade>> cidadesPorZona = new HashMap<>();
        for (ZonaViaturaCidade cidade : cidades) {
            cidadesPorZona.computeIfAbsent(cidade.getZona().getId(), ignored -> new ArrayList<>()).add(cidade);
        }

        Map<Long, Map<Long, List<ViaturaQuadroItemDTO>>> viaturasPorZonaCidade = new HashMap<>();
        List<ViaturaQuadroItemDTO> viaturasSemZona = new ArrayList<>();

        for (ViaturaQuadroItemDTO viatura : viaturas) {
            if (viatura.zonaId() == null) {
                viaturasSemZona.add(viatura);
                continue;
            }

            viaturasPorZonaCidade
                    .computeIfAbsent(viatura.zonaId(), ignored -> new HashMap<>())
                    .computeIfAbsent(viatura.zonaCidadeId(), ignored -> new ArrayList<>())
                    .add(viatura);
        }

        List<ZonaViaturaQuadroDTO> colunas = new ArrayList<>();
        colunas.add(new ZonaViaturaQuadroDTO(
                null,
                "Sem zona",
                "Sem zona",
                true,
                List.of(),
                List.copyOf(viaturasSemZona)
        ));

        for (ZonaViatura zona : zonas) {
            Map<Long, List<ViaturaQuadroItemDTO>> viaturasDaZona =
                    viaturasPorZonaCidade.getOrDefault(zona.getId(), Collections.emptyMap());
            List<ZonaViaturaCidadeQuadroDTO> gruposDeCidade = new ArrayList<>();

            for (ZonaViaturaCidade cidade : cidadesPorZona.getOrDefault(zona.getId(), List.of())) {
                gruposDeCidade.add(new ZonaViaturaCidadeQuadroDTO(
                        cidade.getId(),
                        cidade.getCidade(),
                        cidade.getCidade(),
                        false,
                        List.copyOf(viaturasDaZona.getOrDefault(cidade.getId(), List.of()))
                ));
            }

            List<ViaturaQuadroItemDTO> viaturasSemCidade = viaturasDaZona.getOrDefault(null, List.of());
            if (!viaturasSemCidade.isEmpty()) {
                gruposDeCidade.add(new ZonaViaturaCidadeQuadroDTO(
                        null,
                        "Sem cidade",
                        "Sem cidade",
                        true,
                        List.copyOf(viaturasSemCidade)
                ));
            }

            colunas.add(new ZonaViaturaQuadroDTO(
                    zona.getId(),
                    zona.getNome(),
                    buildZonaLabel(zona.getNome()),
                    false,
                    List.copyOf(gruposDeCidade),
                    List.of()
            ));
        }

        return colunas;
    }

    @Transactional
    public ZonaViaturaResponseDTO criar(ZonaViaturaDTO dto, User user) {
        Batalhao batalhao = requireBatalhao(user);

        ZonaViatura zona = new ZonaViatura();
        zona.setNome(dto.getNome().trim());
        zona.setOrdem(zonaViaturaRepository.findMaxOrdemByBatalhaoId(batalhao.getId()) + 1);
        zona.setBatalhao(batalhao);

        return toResponseDTO(zonaViaturaRepository.save(zona));
    }

    @Transactional
    public ZonaViaturaResponseDTO atualizar(Long id, ZonaViaturaDTO dto, User user) {
        ZonaViatura zona = getOwnedZona(id, requireBatalhaoId(user));
        zona.setNome(dto.getNome().trim());
        return toResponseDTO(zonaViaturaRepository.save(zona));
    }

    @Transactional
    public void deletar(Long id, User user) {
        ZonaViatura zona = getOwnedZona(id, requireBatalhaoId(user));
        viaturaRepository.clearZonaByZonaId(zona.getId());
        zonaViaturaRepository.delete(zona);
    }

    @Transactional
    public void reordenar(ReordenarZonaViaturaDTO dto, User user) {
        Long batalhaoId = requireBatalhaoId(user);
        List<ZonaViatura> zonas = zonaViaturaRepository.findByBatalhaoIdOrderByOrdemAsc(batalhaoId);
        List<Long> zonaIds = dto.getZonaIds();

        if (zonas.size() != zonaIds.size()) {
            throw new AccessDeniedException("Ordem de zonas invalida para o batalhao do usuario.");
        }

        Set<Long> expectedIds = new HashSet<>();
        Map<Long, ZonaViatura> zonasPorId = new HashMap<>();
        for (ZonaViatura zona : zonas) {
            expectedIds.add(zona.getId());
            zonasPorId.put(zona.getId(), zona);
        }

        Set<Long> receivedIds = new HashSet<>(zonaIds);
        if (receivedIds.size() != zonaIds.size() || !expectedIds.equals(receivedIds)) {
            throw new AccessDeniedException("Ordem de zonas invalida para o batalhao do usuario.");
        }

        for (int index = 0; index < zonaIds.size(); index++) {
            ZonaViatura zona = zonasPorId.get(zonaIds.get(index));
            zona.setOrdem(index + 1);
        }

        zonaViaturaRepository.saveAll(zonas);
    }

    @Transactional
    public ZonaViaturaCidadeResponseDTO criarCidade(Long zonaId, ZonaViaturaCidadeDTO dto, User user) {
        ZonaViatura zona = getOwnedZona(zonaId, requireBatalhaoId(user));

        ZonaViaturaCidade cidade = new ZonaViaturaCidade();
        cidade.setCidade(dto.getCidade().trim());
        cidade.setOrdem(zonaViaturaCidadeRepository.findMaxOrdemByZonaId(zona.getId()) + 1);
        cidade.setZona(zona);

        return toCidadeResponseDTO(zonaViaturaCidadeRepository.save(cidade));
    }

    @Transactional
    public ZonaViaturaCidadeResponseDTO atualizarCidade(Long cidadeId, ZonaViaturaCidadeDTO dto, User user) {
        ZonaViaturaCidade cidade = getOwnedCidade(cidadeId, requireBatalhaoId(user));
        cidade.setCidade(dto.getCidade().trim());
        return toCidadeResponseDTO(zonaViaturaCidadeRepository.save(cidade));
    }

    @Transactional
    public void deletarCidade(Long cidadeId, User user) {
        ZonaViaturaCidade cidade = getOwnedCidade(cidadeId, requireBatalhaoId(user));
        viaturaRepository.clearZonaCidadeByZonaCidadeId(cidade.getId());
        zonaViaturaCidadeRepository.delete(cidade);
    }

    private ZonaViatura getOwnedZona(Long id, Long batalhaoId) {
        return zonaViaturaRepository.findByIdAndBatalhaoId(id, batalhaoId)
                .orElseThrow(() -> new EntityNotFoundException("Zona nao encontrada com ID: " + id));
    }

    private ZonaViaturaCidade getOwnedCidade(Long id, Long batalhaoId) {
        return zonaViaturaCidadeRepository.findByIdAndBatalhaoId(id, batalhaoId)
                .orElseThrow(() -> new EntityNotFoundException("Cidade da zona nao encontrada com ID: " + id));
    }

    private ZonaViaturaResponseDTO toResponseDTO(ZonaViatura zona) {
        return new ZonaViaturaResponseDTO(
                zona.getId(),
                zona.getNome(),
                zona.getOrdem(),
                buildZonaLabel(zona.getNome())
        );
    }

    private ZonaViaturaCidadeResponseDTO toCidadeResponseDTO(ZonaViaturaCidade cidade) {
        return new ZonaViaturaCidadeResponseDTO(
                cidade.getId(),
                cidade.getZona().getId(),
                cidade.getCidade(),
                cidade.getOrdem(),
                buildCidadeLabel(cidade.getCidade())
        );
    }

    private String buildZonaLabel(String nome) {
        return nome;
    }

    private String buildCidadeLabel(String cidade) {
        return cidade;
    }

    private Batalhao requireBatalhao(User user) {
        if (user == null || user.getBatalhao() == null || user.getBatalhao().getId() == null) {
            throw new AccessDeniedException("Usuario sem batalhao associado.");
        }

        return user.getBatalhao();
    }

    private Long requireBatalhaoId(User user) {
        return requireBatalhao(user).getId();
    }
}
