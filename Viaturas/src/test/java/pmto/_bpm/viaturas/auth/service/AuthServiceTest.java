package pmto._bpm.viaturas.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pmto._bpm.viaturas.auth.dto.AuthResponse;
import pmto._bpm.viaturas.auth.dto.LoginRequest;
import pmto._bpm.viaturas.auth.dto.RegisterRequest;
import pmto._bpm.viaturas.auth.model.Role;
import pmto._bpm.viaturas.auth.repository.CadastroAutorizadoRepository;
import pmto._bpm.viaturas.batalhao.model.Batalhao;
import pmto._bpm.viaturas.batalhao.repository.BatalhaoRepository;
import pmto._bpm.viaturas.common.exception.RegisterException;
import pmto._bpm.viaturas.users.model.User;
import pmto._bpm.viaturas.users.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private BatalhaoRepository batalhaoRepository;

    @Mock
    private CadastroAutorizadoRepository cadastroAutorizado;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerShouldPersistUserWhenAuthorizedAndUnique() {
        RegisterRequest dto = buildRegisterRequest();
        Batalhao batalhao = buildBatalhao(1L, "8 BPM");

        when(cadastroAutorizado.findByCpfAndMatricula(dto.getCpf(), dto.getMatricula()))
                .thenReturn(Optional.of(mock(CadastroAutorizadoRepository.class)));
        when(batalhaoRepository.findById(dto.getBatalhaoId())).thenReturn(Optional.of(batalhao));
        when(userRepository.existsByCPF(dto.getCpf())).thenReturn(false);
        when(userRepository.existsByMatricula(dto.getMatricula())).thenReturn(false);
        when(passwordEncoder.encode(dto.getSenha())).thenReturn("encoded-password");

        String result = authService.register(dto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User saved = userCaptor.getValue();
        assertEquals("encoded-password", saved.getSenha());
        assertEquals(Role.MOTORISTA, saved.getRole());
        assertEquals("123456", saved.getMatricula());
        assertEquals("Cadastro realizado com sucesso!", result);
    }

    @Test
    void registerShouldThrowWhenCpfAndMatriculaAreNotAuthorized() {
        RegisterRequest dto = buildRegisterRequest();
        Batalhao batalhao = buildBatalhao(1L, "8 BPM");

        when(cadastroAutorizado.findByCpfAndMatricula(dto.getCpf(), dto.getMatricula()))
                .thenReturn(Optional.empty());
        when(batalhaoRepository.findById(dto.getBatalhaoId())).thenReturn(Optional.of(batalhao));

        RegisterException ex = assertThrows(RegisterException.class, () -> authService.register(dto));

        assertNotNull(ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerShouldThrowWhenUserAlreadyExists() {
        RegisterRequest dto = buildRegisterRequest();
        Batalhao batalhao = buildBatalhao(1L, "8 BPM");

        when(cadastroAutorizado.findByCpfAndMatricula(dto.getCpf(), dto.getMatricula()))
                .thenReturn(Optional.of(mock(CadastroAutorizadoRepository.class)));
        when(batalhaoRepository.findById(dto.getBatalhaoId())).thenReturn(Optional.of(batalhao));
        when(userRepository.existsByCPF(dto.getCpf())).thenReturn(true);

        RegisterException ex = assertThrows(RegisterException.class, () -> authService.register(dto));

        assertNotNull(ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginShouldThrowWhenPasswordIsInvalid() {
        LoginRequest dto = new LoginRequest();
        dto.setMatricula("123456");
        dto.setSenha("wrong-password");

        User user = buildUser("123456", "encoded-password");
        when(userRepository.findByMatricula("123456")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        RegisterException ex = assertThrows(RegisterException.class, () -> authService.login(dto));

        assertNotNull(ex.getMessage());
    }

    @Test
    void loginShouldReturnTokenAndUserResponseWhenCredentialsAreValid() {
        LoginRequest dto = new LoginRequest();
        dto.setMatricula("123456");
        dto.setSenha("abc123");

        User user = buildUser("123456", "encoded-password");
        user.setNomeGuerra("Silva");
        user.setRole(Role.MOTORISTA);
        user.setBatalhao(buildBatalhao(1L, "8 BPM"));

        when(userRepository.findByMatricula("123456")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("abc123", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(dto);

        assertEquals("jwt-token", response.getToken());
        assertEquals("123456", response.getUser().getMatricula());
        assertEquals("8 BPM", response.getUser().getBatalhao());
    }

    private RegisterRequest buildRegisterRequest() {
        RegisterRequest dto = new RegisterRequest();
        dto.setNomeGuerra("Silva");
        dto.setGraduacao("SGT");
        dto.setSenha("abc123");
        dto.setCpf("12345678900");
        dto.setMatricula("123456");
        dto.setBatalhaoId(1L);
        return dto;
    }

    private Batalhao buildBatalhao(Long id, String nome) {
        Batalhao batalhao = new Batalhao();
        batalhao.setId(id);
        batalhao.setNome(nome);
        return batalhao;
    }

    private User buildUser(String matricula, String senha) {
        User user = new User();
        user.setMatricula(matricula);
        user.setSenha(senha);
        return user;
    }
}

