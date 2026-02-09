package pmto._bpm.viaturas.users.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pmto._bpm.viaturas.users.model.User;
import pmto._bpm.viaturas.users.dto.ConfirmarUploadDTO;
import pmto._bpm.viaturas.storage.s3.AwsS3Service;
import pmto._bpm.viaturas.users.service.UsuarioService;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final AwsS3Service awsS3Service;
    private final UsuarioService usuarioService;


     // ============================
    // CONFIRMAR FOTO DE PERFIL
    // ============================
    @PostMapping("/me/foto/confirmar")
    public ResponseEntity<Void> confirmarFotoPerfil(
            @AuthenticationPrincipal User usuarioLogado,
            @RequestBody ConfirmarUploadDTO dto
    ) {
        usuarioService.confirmarFotoPerfil(usuarioLogado.getId(), dto.fileKey());
        return ResponseEntity.ok().build();
    }

}
