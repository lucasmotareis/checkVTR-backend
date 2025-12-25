package pmto._bpm.viaturas.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pmto._bpm.viaturas.auth.model.User;
import pmto._bpm.viaturas.dto.ConfirmarUploadDTO;
import pmto._bpm.viaturas.dto.PresignedDownload;
import pmto._bpm.viaturas.service.AwsS3Service;
import pmto._bpm.viaturas.dto.PresignedUpload;
import pmto._bpm.viaturas.service.UsuarioService;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final AwsS3Service awsS3Service;
    private final UsuarioService usuarioService;

    @PostMapping("/me/foto/upload-url")
    public ResponseEntity<PresignedUpload> gerarUploadFotoPerfil(
            @AuthenticationPrincipal User usuarioLogado
    ) {
        PresignedUpload upload =
                awsS3Service.gerarUploadFotoPerfil(usuarioLogado.getId());

        return ResponseEntity.ok(upload);
    }

    @PostMapping("/me/cnh/upload-url")
    public ResponseEntity<PresignedUpload> gerarUploadCnh(
            @AuthenticationPrincipal User usuarioLogado

    ) {
        PresignedUpload upload =
                awsS3Service.gerarUploadCnh(usuarioLogado.getId());

        return ResponseEntity.ok(upload);
    }

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

    // ============================
    // CONFIRMAR CNH
    // ============================
    @PostMapping("/me/cnh/confirmar")
    public ResponseEntity<Void> confirmarCnh(
            @AuthenticationPrincipal User usuarioLogado,
            @RequestBody ConfirmarUploadDTO dto
    ) {
        usuarioService.confirmarCnh(usuarioLogado.getId(), dto.fileKey());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/usuarios/{id}/cnh")
    @PreAuthorize("hasRole('CHEFE_TRANSPORTE')")
    public ResponseEntity<PresignedDownload> baixarCnh(
            @PathVariable Long id
    ) {
        PresignedDownload download =
                awsS3Service.gerarDownloadCnh(id);

        return ResponseEntity.ok(download);
    }
}
