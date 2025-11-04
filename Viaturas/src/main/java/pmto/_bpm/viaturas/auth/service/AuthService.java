package pmto._bpm.viaturas.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pmto._bpm.viaturas.auth.dto.AuthResponse;
import pmto._bpm.viaturas.auth.dto.LoginRequest;
import pmto._bpm.viaturas.auth.dto.RegisterRequest;
import pmto._bpm.viaturas.auth.dto.UserResponse;
import pmto._bpm.viaturas.auth.model.Role;
import pmto._bpm.viaturas.auth.model.User;
import pmto._bpm.viaturas.auth.repository.CadastroAutorizadoRepository;
import pmto._bpm.viaturas.auth.repository.UserRepository;
import pmto._bpm.viaturas.model.Batalhao;
import pmto._bpm.viaturas.repository.BatalhaoRepository;

import java.util.Optional;


@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BatalhaoRepository batalhaoRepository;
    private final CadastroAutorizadoRepository cadastroAutorizado;
    private final JwtService jwtService;


    public AuthService(BatalhaoRepository batalhaoRepository, UserRepository userRepository, PasswordEncoder passwordEncoder, CadastroAutorizadoRepository cadastroAutorizado, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cadastroAutorizado = cadastroAutorizado;
        this.batalhaoRepository = batalhaoRepository;
        this.jwtService = jwtService;
    }

    public String register(RegisterRequest dto) {
        Optional<CadastroAutorizadoRepository> autorizado =
                cadastroAutorizado.findByCpfAndMatricula(dto.getCpf(), dto.getMatricula());

        Batalhao batalhao = batalhaoRepository.findById(dto.getBatalhaoId())
                .orElseThrow(() -> new IllegalArgumentException("Batalhão não encontrado"));

        if (autorizado.isEmpty()) {
            throw new IllegalArgumentException("CPF e Matrícula não autorizados.");
        }

        if (userRepository.existsByCPF(dto.getCpf()) || userRepository.existsByMatricula(dto.getMatricula())) {
            throw new IllegalArgumentException("Usuário já cadastrado. Entre em contato com o Suporte.");
        }

        User user = new User();
        user.setMatricula( dto.getMatricula() );
        user.setCPF(dto.getCpf() );
        user.setSenha(passwordEncoder.encode(dto.getSenha()));
        user.setNome_guerra( dto.getNome_guerra() );
        user.setRole(Role.valueOf("MOTORISTA"));
        user.setBatalhao(batalhao);
        userRepository.save(user);

        return "Cadastro realizado com sucesso!";

    }

    public AuthResponse login(LoginRequest dto) {
        User user = userRepository.findByMatricula(dto.getMatricula())
                .orElseThrow(()-> new IllegalArgumentException("Usuário não encontrado."));

        if (!passwordEncoder.matches(dto.getSenha(), user.getSenha())) {
            throw new IllegalArgumentException("Senha inválida.");
        }
        String token = jwtService.generateToken(user);

        return new AuthResponse(token, new UserResponse(user.getBatalhao().getId(),user.getNome_guerra(),user.getBatalhao().getNome()
        ,user.getRole().toString(),user.getMatricula()));


    }


}
