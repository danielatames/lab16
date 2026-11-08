package ucr.ac.cr.medivault.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ucr.ac.cr.medivault.business.SecurityAuditorService;
import ucr.ac.cr.medivault.security.ClientIpResolver;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalSecurityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalSecurityExceptionHandler.class);
    private final SecurityAuditorService auditor;

    public GlobalSecurityExceptionHandler(SecurityAuditorService auditor) {
        this.auditor = auditor;
    }

    private ProblemDetail problema(HttpStatus status, String title, String detail, String type) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(status, detail);
        p.setTitle(title);
        p.setType(URI.create("https://medivault.ucr.ac.cr/" + type));
        return p;
    }

    @ExceptionHandler(AccessDeniedSecurityException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedSecurityException ex, HttpServletRequest req) {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        String user = (a != null) ? a.getName() : "anonimo";
        String detalle = (ex.getExpedienteId() != null)
                ? "Expediente intentado: " + ex.getExpedienteId()
                : "Acceso denegado";
        auditor.registrar(user, ClientIpResolver.resolve(req), "IDOR_ATTEMPT_BLOCKED",
                req.getRequestURI(), 403, detalle);
        return problema(HttpStatus.FORBIDDEN, "Acceso Prohibido (IDOR / BOLA)",
                ex.getMessage(), "forbidden");
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ProblemDetail handleSpringAccessDenied(
            org.springframework.security.access.AccessDeniedException ex) {
        return problema(HttpStatus.FORBIDDEN, "Acceso Prohibido",
                "No tiene permisos para este recurso.", "forbidden");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return problema(HttpStatus.NOT_FOUND, "Recurso No Encontrado", ex.getMessage(), "not-found");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        return problema(HttpStatus.UNAUTHORIZED, "Fallo de Autenticacion",
                "Credenciales invalidas.", "unauthorized");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail p = problema(HttpStatus.BAD_REQUEST, "Error de Validacion",
                "Validacion de datos fallida.", "bad-request");
        Map<String, String> errors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        p.setProperty("invalid_fields", errors);
        return p;
    }

    @ExceptionHandler({ HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class })
    public ProblemDetail handleBadInput(Exception ex) {
        return problema(HttpStatus.BAD_REQUEST, "Solicitud Invalida",
                "Datos de entrada invalidos.", "bad-request");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleConflict(DataIntegrityViolationException ex) {
        log.warn("Conflicto de integridad de datos");
        return problema(HttpStatus.CONFLICT, "Conflicto de Datos",
                "La operacion viola una restriccion de datos.", "conflict");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        log.error("Error interno no controlado", ex);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Error Interno",
                "Error interno del servidor.", "internal");
    }
}