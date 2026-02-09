package pmto._bpm.viaturas.users.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pmto._bpm.viaturas.users.model.User;
import pmto._bpm.viaturas.common.exception.RegisterException;
import pmto._bpm.viaturas.users.repository.UserRepository;


@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UserRepository userRepository;

    public void confirmarFotoPerfil(Long userId, String fileKey) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RegisterException("Usuário não encontrado"));
        String publicUrl = "https://usuarios-perfil.s3.sa-east-1.amazonaws.com/" + fileKey;
        user.setFotoPerfilUrl(publicUrl);
        userRepository.save(user);
    }

}
