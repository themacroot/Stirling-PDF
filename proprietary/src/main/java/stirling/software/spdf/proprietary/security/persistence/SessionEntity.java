package stirling.software.spdf.proprietary.security.persistence;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "sessions")
public class SessionEntity implements Serializable {
    @Id private String sessionId;

    private String principalName;

    private Date lastRequest;

    private boolean expired;
}
