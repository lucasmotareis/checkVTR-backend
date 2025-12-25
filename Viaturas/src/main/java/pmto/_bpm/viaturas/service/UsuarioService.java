package pmto._bpm.viaturas.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pmto._bpm.viaturas.auth.model.User;
import pmto._bpm.viaturas.exception.RegisterException;
import pmto._bpm.viaturas.auth.repository.UserRepository;


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

    public void confirmarCnh(Long userId, String fileKey) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RegisterException("Usuário não encontrado"));

        user.setCnhKey(fileKey);
        userRepository.save(user);
    }
}
