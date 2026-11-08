package ucr.ac.cr.medivault.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ucr.ac.cr.medivault.controller.dto.AuditLogResponseDto;
import ucr.ac.cr.medivault.data.AuditLogRepository;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auditoria")
public class AuditoriaController {
    private final AuditLogRepository repo;

    public AuditoriaController(AuditLogRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<List<AuditLogResponseDto>> ultimosEventos() {
        return ResponseEntity.ok(repo.findTop50ByOrderByFechaEventoDesc().stream()
                .map(a -> new AuditLogResponseDto(a.getId(), a.getUsername(), a.getIpOrigen(),
                        a.getAccion(), a.getRecursoSolicitado(), a.getCodigoEstado(),
                        a.getDetalleEvento(), a.getFechaEvento()))
                .toList());
    }
}