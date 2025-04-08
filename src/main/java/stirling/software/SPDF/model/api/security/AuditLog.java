package stirling.software.SPDF.model.api.security;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String method;
    private String endpoint;

    @Lob
    private String requestParams;  // text form params in JSON
    private String userAgent;
    private String ipAddress;
    private String username;
    private LocalDateTime timestamp;

    // Store file metadata (comma or semicolon-separated)
    @Lob
    private String fileNames;

}
