package pmto._bpm.viaturas.viaturas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import pmto._bpm.viaturas.batalhao.model.Batalhao;
import pmto._bpm.viaturas.users.model.User;
import pmto._bpm.viaturas.viaturas.dto.ReordenarZonaViaturaDTO;
import pmto._bpm.viaturas.viaturas.dto.ViaturaQuadroItemDTO;
import pmto._bpm.viaturas.viaturas.dto.ZonaViaturaCidadeDTO;
import pmto._bpm.viaturas.viaturas.dto.ZonaViaturaDTO;
import pmto._bpm.viaturas.viaturas.dto.ZonaViaturaQuadroDTO;
import pmto._bpm.viaturas.viaturas.dto.ZonaViaturaResponseDTO;
import pmto._bpm.viaturas.viaturas.model.ZonaViaturaCidade;
import pmto._bpm.viaturas.viaturas.model.ZonaViatura;
import pmto._bpm.viaturas.viaturas.repository.ZonaViaturaCidadeRepository;
import pmto._bpm.viaturas.viaturas.repository.ViaturaRepository;
import pmto._bpm.viaturas.viaturas.repository.ZonaViaturaRepository;

@ExtendWith(MockitoExtension.class)
class ZonaViaturaServiceTest {

    @Mock
    private ZonaViaturaRepository zonaViaturaRepository;

    @Mock
    private ViaturaRepository viaturaRepository;

    @Mock
    private ZonaViaturaCidadeRepository zonaViaturaCidadeRepository;

    @InjectMocks
    private ZonaViaturaService zonaViaturaService;

    @Test
    void criarShouldAssignNextOrderForAuthenticatedBattalion() {
        Batalhao batalhao = new Batalhao();
        batalhao.setId(7L);

        User user = new User();
        user.setBatalhao(batalhao);

        ZonaViaturaDTO dto = new ZonaViaturaDTO();
        dto.setNome("2a CIA");

        when(zonaViaturaRepository.findMaxOrdemByBatalhaoId(7L)).thenReturn(3);
        when(zonaViaturaRepository.save(any(ZonaViatura.class))).thenAnswer(invocation -> {
            ZonaViatura zona = invocation.getArgument(0);
            zona.setId(9L);
            return zona;
        });

        ZonaViaturaResponseDTO created = zonaViaturaService.criar(dto, user);

        assertEquals(9L, created.id());
        assertEquals(4, created.ordem());
        assertEquals("2a CIA", created.label());
    }

    @Test
    void deletarShouldMoveVehiclesToSemZonaBeforeDelete() {
        Batalhao batalhao = new Batalhao();
        batalhao.setId(7L);

        User user = new User();
        user.setBatalhao(batalhao);

        ZonaViatura zona = new ZonaViatura();
        zona.setId(4L);
        zona.setBatalhao(batalhao);

        when(zonaViaturaRepository.findByIdAndBatalhaoId(4L, 7L)).thenReturn(java.util.Optional.of(zona));

        zonaViaturaService.deletar(4L, user);

        verify(viaturaRepository).clearZonaByZonaId(4L);
        verify(zonaViaturaRepository).delete(zona);
    }

    @Test
    void listarQuadroShouldAlwaysIncludeSemZonaColumn() {
        Batalhao batalhao = new Batalhao();
        batalhao.setId(7L);

        User user = new User();
        user.setBatalhao(batalhao);

        ZonaViatura zona = new ZonaViatura();
        zona.setId(1L);
        zona.setNome("1a CIA");
        zona.setOrdem(1);
        zona.setBatalhao(batalhao);

        ZonaViaturaCidade cidade = new ZonaViaturaCidade();
        cidade.setId(15L);
        cidade.setCidade("Paraiso");
        cidade.setOrdem(1);
        cidade.setZona(zona);

        when(zonaViaturaRepository.findByBatalhaoIdOrderByOrdemAsc(7L)).thenReturn(List.of(zona));
        when(zonaViaturaCidadeRepository.findByBatalhaoIdOrderByZonaOrdemAscOrdemAsc(7L))
                .thenReturn(List.of(cidade));
        when(viaturaRepository.findViaturasParaQuadro(7L)).thenReturn(List.of(
                new ViaturaQuadroItemDTO(11L, "PM-001", "Hilux", "ABC1234", false, 1000, 2000, 80, 0L, null, null),
                new ViaturaQuadroItemDTO(12L, "PM-002", "Hilux", "ABC5678", false, 2000, 3000, 70, 1L, 1L, 15L)
        ));

        List<ZonaViaturaQuadroDTO> quadro = zonaViaturaService.listarQuadro(user);

        assertEquals(2, quadro.size());
        assertEquals("Sem zona", quadro.get(0).label());
        assertEquals(1, quadro.get(0).viaturas().size());
        assertEquals("1a CIA", quadro.get(1).label());
        assertEquals(1, quadro.get(1).cidades().size());
        assertEquals("Paraiso", quadro.get(1).cidades().get(0).cidade());
        assertEquals(1, quadro.get(1).cidades().get(0).viaturas().size());
    }

    @Test
    void listarQuadroShouldHandleZoneWithoutCitiesOrVehicles() {
        Batalhao batalhao = new Batalhao();
        batalhao.setId(7L);

        User user = new User();
        user.setBatalhao(batalhao);

        ZonaViatura zona = new ZonaViatura();
        zona.setId(1L);
        zona.setNome("2a CIA");
        zona.setOrdem(1);
        zona.setBatalhao(batalhao);

        when(zonaViaturaRepository.findByBatalhaoIdOrderByOrdemAsc(7L)).thenReturn(List.of(zona));
        when(zonaViaturaCidadeRepository.findByBatalhaoIdOrderByZonaOrdemAscOrdemAsc(7L))
                .thenReturn(List.of());
        when(viaturaRepository.findViaturasParaQuadro(7L)).thenReturn(List.of());

        List<ZonaViaturaQuadroDTO> quadro = zonaViaturaService.listarQuadro(user);

        assertEquals(2, quadro.size());
        assertEquals("Sem zona", quadro.get(0).label());
        assertEquals("2a CIA", quadro.get(1).label());
        assertEquals(0, quadro.get(1).cidades().size());
        assertEquals(0, quadro.get(1).viaturas().size());
    }

    @Test
    void criarShouldRequireBattalionOnAuthenticatedUser() {
        User user = new User();
        ZonaViaturaDTO dto = new ZonaViaturaDTO();
        dto.setNome("2a CIA");

        assertThrows(AccessDeniedException.class, () -> zonaViaturaService.criar(dto, user));
    }

    @Test
    void reordenarShouldPersistNewZoneOrder() {
        Batalhao batalhao = new Batalhao();
        batalhao.setId(7L);

        User user = new User();
        user.setBatalhao(batalhao);

        ZonaViatura zonaUm = new ZonaViatura();
        zonaUm.setId(1L);
        zonaUm.setOrdem(1);
        zonaUm.setBatalhao(batalhao);

        ZonaViatura zonaDois = new ZonaViatura();
        zonaDois.setId(2L);
        zonaDois.setOrdem(2);
        zonaDois.setBatalhao(batalhao);

        when(zonaViaturaRepository.findByBatalhaoIdOrderByOrdemAsc(7L)).thenReturn(List.of(zonaUm, zonaDois));

        ReordenarZonaViaturaDTO dto = new ReordenarZonaViaturaDTO();
        dto.setZonaIds(List.of(2L, 1L));

        zonaViaturaService.reordenar(dto, user);

        assertEquals(2, zonaUm.getOrdem());
        assertEquals(1, zonaDois.getOrdem());
        verify(zonaViaturaRepository).saveAll(any());
    }

    @Test
    void reordenarShouldRejectInvalidZoneList() {
        Batalhao batalhao = new Batalhao();
        batalhao.setId(7L);

        User user = new User();
        user.setBatalhao(batalhao);

        ZonaViatura zonaUm = new ZonaViatura();
        zonaUm.setId(1L);
        zonaUm.setBatalhao(batalhao);

        when(zonaViaturaRepository.findByBatalhaoIdOrderByOrdemAsc(7L)).thenReturn(List.of(zonaUm));

        ReordenarZonaViaturaDTO dto = new ReordenarZonaViaturaDTO();
        dto.setZonaIds(List.of(99L));

        assertThrows(AccessDeniedException.class, () -> zonaViaturaService.reordenar(dto, user));
    }

    @Test
    void criarCidadeShouldAssignNextOrderInsideZone() {
        Batalhao batalhao = new Batalhao();
        batalhao.setId(7L);

        User user = new User();
        user.setBatalhao(batalhao);

        ZonaViatura zona = new ZonaViatura();
        zona.setId(9L);
        zona.setNome("2a CIA");
        zona.setBatalhao(batalhao);

        ZonaViaturaCidadeDTO dto = new ZonaViaturaCidadeDTO();
        dto.setCidade("Divinopolis");

        when(zonaViaturaRepository.findByIdAndBatalhaoId(9L, 7L)).thenReturn(java.util.Optional.of(zona));
        when(zonaViaturaCidadeRepository.findMaxOrdemByZonaId(9L)).thenReturn(2);
        when(zonaViaturaCidadeRepository.save(any(ZonaViaturaCidade.class))).thenAnswer(invocation -> {
            ZonaViaturaCidade cidade = invocation.getArgument(0);
            cidade.setId(15L);
            return cidade;
        });

        var created = zonaViaturaService.criarCidade(9L, dto, user);

        assertEquals(15L, created.id());
        assertEquals(9L, created.zonaId());
        assertEquals(3, created.ordem());
        assertEquals("Divinopolis", created.label());
    }

    @Test
    void deletarCidadeShouldClearOnlyCityBinding() {
        Batalhao batalhao = new Batalhao();
        batalhao.setId(7L);

        User user = new User();
        user.setBatalhao(batalhao);

        ZonaViatura zona = new ZonaViatura();
        zona.setId(4L);
        zona.setBatalhao(batalhao);

        ZonaViaturaCidade cidade = new ZonaViaturaCidade();
        cidade.setId(6L);
        cidade.setZona(zona);

        when(zonaViaturaCidadeRepository.findByIdAndBatalhaoId(6L, 7L))
                .thenReturn(java.util.Optional.of(cidade));

        zonaViaturaService.deletarCidade(6L, user);

        verify(viaturaRepository).clearZonaCidadeByZonaCidadeId(6L);
        verify(zonaViaturaCidadeRepository).delete(cidade);
    }
}
