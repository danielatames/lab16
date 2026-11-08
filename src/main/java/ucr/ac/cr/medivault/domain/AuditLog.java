package ucr.ac.cr.medivault.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import java.time.LocalDateTime;


@Entity
@Immutable
@Table(name = "AUDIT_LOGS")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", updatable = false, length = 50)
    private String username;

    @Column(name = "ip_origen", nullable = false, updatable = false, length = 45)
    private String ipOrigen;

    @Column(nullable = false, updatable = false, length = 50)
    private String accion;

    @Column(name = "recurso_solicitado", nullable = false, updatable = false)
    private String recursoSolicitado;

    @Column(name = "codigo_estado", nullable = false, updatable = false)
    private int codigoEstado;

    @Column(name = "detalle_evento", updatable = false)
    private String detalleEvento;

    @Column(name = "fecha_evento", updatable = false)
    private LocalDateTime fechaEvento;

    protected AuditLog() {
    }

    public AuditLog(String username, String ipOrigen, String accion,
            String recursoSolicitado, int codigoEstado, String detalleEvento) {
        this.username = username;
        this.ipOrigen = ipOrigen;
        this.accion = accion;
        this.recursoSolicitado = recursoSolicitado;
        this.codigoEstado = codigoEstado;
        this.detalleEvento = detalleEvento;
        this.fechaEvento = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getIpOrigen() {
        return ipOrigen;
    }

    public String getAccion() {
        return accion;
    }

    public String getRecursoSolicitado() {
        return recursoSolicitado;
    }

    public int getCodigoEstado() {
        return codigoEstado;
    }

    public String getDetalleEvento() {
        return detalleEvento;
    }

    public LocalDateTime getFechaEvento() {
        return fechaEvento;
    }
}