package pmto._bpm.viaturas.checklists.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pmto._bpm.viaturas.checklists.model.CheckList;
import pmto._bpm.viaturas.checklists.repository.CheckListRepository;
import pmto._bpm.viaturas.checklists.repository.ProblemaRepository;
import pmto._bpm.viaturas.feed.service.FeedService;
import pmto._bpm.viaturas.viaturas.repository.ViaturaRepository;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckListServiceTest {

    @Mock
    private CheckListRepository checkListRepository;

    @Mock
    private ViaturaRepository viaturaRepository;

    @Mock
    private ProblemaRepository problemaRepository;

    @Mock
    private FeedService feedService;

    @InjectMocks
    private CheckListService checkListService;

    @Test
    void marcarVistoPeloChefeShouldMarkWhenNotSeen() {
        CheckList checklist = new CheckList();
        checklist.setId(1L);
        checklist.setVistoPeloChefe(false);

        when(checkListRepository.findById(1L)).thenReturn(Optional.of(checklist));

        checkListService.marcarVistoPeloChefe(1L);

        assertTrue(checklist.isVistoPeloChefe());
        assertNotNull(checklist.getVistoPeloChefeEm());
    }

    @Test
    void marcarVistoPeloChefeShouldKeepTimestampWhenAlreadySeen() {
        Instant seenAt = Instant.parse("2026-01-05T10:15:30Z");
        CheckList checklist = new CheckList();
        checklist.setId(2L);
        checklist.setVistoPeloChefe(true);
        checklist.setVistoPeloChefeEm(seenAt);

        when(checkListRepository.findById(2L)).thenReturn(Optional.of(checklist));

        checkListService.marcarVistoPeloChefe(2L);

        assertTrue(checklist.isVistoPeloChefe());
        assertEquals(seenAt, checklist.getVistoPeloChefeEm());
    }
}

