package pmto._bpm.viaturas.users.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pmto._bpm.viaturas.users.model.User;
import pmto._bpm.viaturas.common.exception.RegisterException;
import pmto._bpm.viaturas.users.repository.UserRepository;
import pmto._bpm.viaturas.storage.s3.S3StorageProperties;


@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UserRepository userRepository;
    private final S3StorageProperties storageProperties;

    public void confirmarFotoPerfil(Long userId, String fileKey) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RegisterException("Usuário não encontrado"));
        String publicUrl = storageProperties.profilePublicUrl(fileKey);
        user.setFotoPerfilUrl(publicUrl);
        userRepository.save(user);
    }

}
