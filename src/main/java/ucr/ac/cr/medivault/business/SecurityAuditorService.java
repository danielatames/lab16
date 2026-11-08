package ucr.ac.cr.medivault.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ucr.ac.cr.medivault.data.AuditLogRepository;
import ucr.ac.cr.medivault.domain.AuditLog;

@Service
public class SecurityAuditorService {
    private static final Logger log = LoggerFactory.getLogger(SecurityAuditorService.class);
    private final AuditLogRepository repo;

    public SecurityAuditorService(AuditLogRepository repo) {
        this.repo = repo;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String username, String ip, String accion,
            String recurso, int codigoEstado, String detalle) {
        try {
            repo.save(new AuditLog(cortar(username, 50), cortar(ip, 45), accion,
                    cortar(recurso, 255), codigoEstado, cortar(detalle, 255)));
        } catch (Exception e) {
            log.error("No se pudo registrar el evento de auditoria {}", accion);
        }
    }

    private String cortar(String s, int max) {
        if (s == null)
            return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}