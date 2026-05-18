package pmto._bpm.viaturas.viaturas.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import pmto._bpm.viaturas.batalhao.model.Batalhao;
import pmto._bpm.viaturas.users.model.User;
import pmto._bpm.viaturas.viaturas.dto.ViaturaByIdDTO;
import pmto._bpm.viaturas.viaturas.dto.ViaturaDTO;
import pmto._bpm.viaturas.viaturas.dto.AtualizarZonaViaturaDTO;
import pmto._bpm.viaturas.viaturas.model.Viatura;
import pmto._bpm.viaturas.viaturas.model.ZonaViaturaCidade;
import pmto._bpm.viaturas.viaturas.model.ZonaViatura;
import pmto._bpm.viaturas.viaturas.repository.ZonaViaturaCidadeRepository;
import pmto._bpm.viaturas.viaturas.repository.ViaturaRepository;
import pmto._bpm.viaturas.viaturas.repository.ZonaViaturaRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViaturaServiceTest {

    @Mock
    private ViaturaRepository viaturaRepository;

    @Mock
    private ZonaViaturaRepository zonaViaturaRepository;

    @Mock
    private ZonaViaturaCidadeRepository zonaViaturaCidadeRepository;

    @InjectMocks
    private ViaturaService viaturaService;

    @Test
    void saveShouldPersistViaturaWhenAuthenticatedUserHasBatalhao() {
        Batalhao batalhao = new Batalhao();
        batalhao.setId(1L);
        batalhao.setNome("8 BPM");

        User user = new User();
        user.setBatalhao(batalhao);

        ViaturaDTO dto = new ViaturaDTO();
        dto.setPlaca("ABC-1234");
        dto.setPrefixo("PM-01");
        dto.setModelo("Hilux");
        dto.setKmAtual(1000);
        dto.setKmRevisao(2000);
        dto.setCombustivelAtualPercentual(75);
        dto.setManutencao(false);

        when(viaturaRepository.save(any(Viatura.class))).thenAnswer(invocation -> {
            Viatura v = invocation.getArgument(0);
            v.setId(10L);
            return v;
        });

        Viatura saved = viaturaService.save(dto, user);

        assertNotNull(saved.getId());
        assertEquals("ABC-1234", saved.getPlaca());
        assertEquals("PM-01", saved.getPrefixo());
        assertEquals(1L, saved.getBatalhao().getId());
        assertEquals(75, saved.getCombustivelAtualPercentual());
        verify(viaturaRepository).save(any(Viatura.class));
    }

    @Test
    void saveShouldDenyWhenAuthenticatedUserHasNoBatalhao() {
        User user = new User();
        ViaturaDTO dto = new ViaturaDTO();
        dto.setPlaca("ABC-1234");
        dto.setPrefixo("PM-01");
        dto.setModelo("Hilux");
        dto.setKmAtual(1000);
        dto.setKmRevisao(2000);

        assertThrows(AccessDeniedException.class, () -> viaturaService.save(dto, user));
        verify(viaturaRepository, never()).save(any(Viatura.class));
    }

    @Test
    void deleteShouldThrowWhenViaturaDoesNotExist() {
        when(viaturaRepository.existsById(999L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> viaturaService.delete(999L));
        verify(viaturaRepository, never()).deleteById(999L);
    }

    @Test
    void listarViaturasDTOShouldMapEntityFields() {
        Batalhao batalhao = new Batalhao();
        batalhao.setId(5L);
        batalhao.setNome("6 BPM");

        Viatura viatura = new Viatura();
        viatura.setId(2L);
        viatura.setPrefixo("PM-20");
        viatura.setPlaca("QWE-1234");
        viatura.setModelo("Ranger");
        viatura.setKmAtual(500);
        viatura.setKmRevisao(1500);
        viatura.setCombustivelAtualPercentual(40);
        viatura.setManutencao(true);
        viatura.setBatalhao(batalhao);

        when(viaturaRepository.findByBatalhaoId(5L)).thenReturn(List.of(viatura));

        List<ViaturaByIdDTO> result = viaturaService.listarViaturasDTO(5L);

        assertEquals(1, result.size());
        ViaturaByIdDTO dto = result.get(0);
        assertEquals(2L, dto.id());
        assertEquals("PM-20", dto.prefixo());
        assertEquals(40, dto.combustivelAtualPercentual());
        assertEquals(5L, dto.batalhaoId());
        assertEquals("6 BPM", dto.batalhaoNome());
    }

    @Test
    void atualizarZonaShouldRejectZoneFromAnotherBattalion() {
        Batalhao userBatalhao = new Batalhao();
        userBatalhao.setId(8L);

        Batalhao otherBatalhao = new Batalhao();
        otherBatalhao.setId(9L);

        User user = new User();
        user.setBatalhao(userBatalhao);

        Viatura viatura = new Viatura();
        viatura.setId(10L);
        viatura.setBatalhao(userBatalhao);

        ZonaViatura zona = new ZonaViatura();
        zona.setId(30L);
        zona.setBatalhao(otherBatalhao);

        AtualizarZonaViaturaDTO dto = new AtualizarZonaViaturaDTO();
        dto.setZonaId(30L);

        when(viaturaRepository.findByIdForUpdate(10L)).thenReturn(java.util.Optional.of(viatura));
        when(zonaViaturaRepository.findById(30L)).thenReturn(java.util.Optional.of(zona));

        assertThrows(AccessDeniedException.class, () -> viaturaService.atualizarZona(10L, dto, user));
    }

    @Test
    void atualizarZonaShouldClearZoneWhenZonaIdIsNull() {
        Batalhao batalhao = new Batalhao();
        batalhao.setId(8L);

        User user = new User();
        user.setBatalhao(batalhao);

        Viatura viatura = new Viatura();
        viatura.setId(10L);
        viatura.setBatalhao(batalhao);

        ZonaViatura zona = new ZonaViatura();
        zona.setId(3L);
        viatura.setZona(zona);

        AtualizarZonaViaturaDTO dto = new AtualizarZonaViaturaDTO();

        when(viaturaRepository.findByIdForUpdate(10L)).thenReturn(java.util.Optional.of(viatura));
        when(viaturaRepository.save(any(Viatura.class))).thenAnswer(invocation -> invocation.getArgument(0));

        viaturaService.atualizarZona(10L, dto, user);

        assertEquals(null, viatura.getZona());
        assertEquals(null, viatura.getZonaCidade());
        verify(viaturaRepository).save(viatura);
    }

    @Test
    void atualizarZonaShouldAssignCityAndParentZone() {
        Batalhao batalhao = new Batalhao();
        batalhao.setId(8L);

        User user = new User();
        user.setBatalhao(batalhao);

        Viatura viatura = new Viatura();
        viatura.setId(10L);
        viatura.setBatalhao(batalhao);

        ZonaViatura zona = new ZonaViatura();
        zona.setId(3L);
        zona.setBatalhao(batalhao);

        ZonaViaturaCidade zonaCidade = new ZonaViaturaCidade();
        zonaCidade.setId(5L);
        zonaCidade.setZona(zona);

        AtualizarZonaViaturaDTO dto = new AtualizarZonaViaturaDTO();
        dto.setZonaId(3L);
        dto.setZonaCidadeId(5L);

        when(viaturaRepository.findByIdForUpdate(10L)).thenReturn(java.util.Optional.of(viatura));
        when(zonaViaturaCidadeRepository.findById(5L))
                .thenReturn(java.util.Optional.of(zonaCidade));
        when(viaturaRepository.save(any(Viatura.class))).thenAnswer(invocation -> invocation.getArgument(0));

        viaturaService.atualizarZona(10L, dto, user);

        assertEquals(zona, viatura.getZona());
        assertEquals(zonaCidade, viatura.getZonaCidade());
    }
}
