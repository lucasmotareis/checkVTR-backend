package pmto._bpm.viaturas.checklists.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import pmto._bpm.viaturas.auth.model.Role;
import pmto._bpm.viaturas.auth.security.JwtAuthFilter;
import pmto._bpm.viaturas.batalhao.model.Batalhao;
import pmto._bpm.viaturas.checklists.dto.CheckListResponseDTO;
import pmto._bpm.viaturas.checklists.model.CheckList;
import pmto._bpm.viaturas.checklists.service.CheckListService;
import pmto._bpm.viaturas.testsupport.TestSecurityConfig;
import pmto._bpm.viaturas.users.model.User;
import pmto._bpm.viaturas.viaturas.service.ViaturaService;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CheckListController.class)
@Import(TestSecurityConfig.class)
class CheckListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CheckListService checkListService;

    @MockBean
    private ViaturaService viaturaService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void allowFilterChain() throws Exception {
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    @Test
    void postChecklistShouldReturnCheckListResponseDTO() throws Exception {
        CheckList checkList = new CheckList();
        checkList.setId(10L);

        CheckListResponseDTO responseDTO = new CheckListResponseDTO();
        responseDTO.setId(10L);
        responseDTO.setPrefixo("PM-10");
        responseDTO.setKmAtual(1500);

        when(checkListService.criar(any(), any(User.class))).thenReturn(checkList);
        when(checkListService.toDTO(checkList)).thenReturn(responseDTO);

        String body = objectMapper.writeValueAsString(Map.of(
                "viaturaId", 1,
                "problemas", List.of(Map.of("problemaId", 1, "observacao", "ok")),
                "kmAtual", 1500,
                "kmRevisao", 2000,
                "imagens", List.of("https://img/1.jpg")
        ));

        mockMvc.perform(post("/checklist")
                        .with(authentication(auth(Role.MOTORISTA, 1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.prefixo").value("PM-10"));
    }

    @Test
    void postChecklistVistoShouldRequireChiefRoleAndReturnNoContent() throws Exception {
        mockMvc.perform(post("/checklist/22/visto")
                        .with(authentication(auth(Role.MOTORISTA, 1L))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/checklist/22/visto")
                        .with(authentication(auth(Role.CHEFE_TRANSPORTE, 1L))))
                .andExpect(status().isNoContent());

        verify(checkListService).marcarVistoPeloChefe(22L);
    }

    @Test
    void getChecklistsByViaturaShouldReturnPagedDto() throws Exception {
        CheckListResponseDTO dto = new CheckListResponseDTO();
        dto.setId(30L);
        dto.setPrefixo("PM-30");
        dto.setNomeGuerra("Silva");

        when(checkListService.findByViaturaId(eq(5L), any(), any(User.class)))
                .thenReturn(new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/checklist/viaturas/5/checklists")
                        .param("page", "0")
                        .param("size", "20")
                        .with(authentication(auth(Role.MOTORISTA, 1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(30))
                .andExpect(jsonPath("$.content[0].prefixo").value("PM-30"));
    }

    @Test
    void postChecklistShouldReturnBadRequestWhenPayloadInvalid() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "viaturaId", 1,
                "problemas", List.of(),
                "kmAtual", 0,
                "kmRevisao", 0,
                "imagens", List.of()
        ));

        mockMvc.perform(post("/checklist")
                        .with(authentication(auth(Role.MOTORISTA, 1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(checkListService, never()).criar(any(), any(User.class));
    }

    private UsernamePasswordAuthenticationToken auth(Role role, Long batalhaoId) {
        Batalhao batalhao = new Batalhao();
        batalhao.setId(batalhaoId);
        batalhao.setNome("8 BPM");

        User user = new User();
        user.setNomeGuerra("Silva");
        user.setMatricula("123456");
        user.setRole(role);
        user.setBatalhao(batalhao);

        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }
}
