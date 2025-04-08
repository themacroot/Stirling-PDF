package stirling.software.SPDF.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import stirling.software.SPDF.model.api.security.AuditLog;
import stirling.software.SPDF.repository.AuditLogRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public void logRequest(
        String method,
        String endpoint,
        Map<String, String[]> requestParams,
        String userAgent,
        String ipAddress,
        String username,
        MultipartFile[] files
    ) {
        try {
            // Build the AuditLog entity
            AuditLog auditLog = new AuditLog();
            auditLog.setMethod(method);
            auditLog.setEndpoint(endpoint);

            // Convert all text form fields (key -> array of values) to JSON for readability
            String paramsJson = requestParams != null && !requestParams.isEmpty()
                ? objectMapper.writeValueAsString(requestParams)
                : null;
            auditLog.setRequestParams(paramsJson);

            auditLog.setUserAgent(userAgent);
            auditLog.setIpAddress(ipAddress);
            auditLog.setUsername(username);
            auditLog.setTimestamp(LocalDateTime.now());

            // Only log metadata for files
            if (files != null && files.length > 0) {
                String fileNamesStr = Arrays.stream(files)
                    .map(file -> String.format(
                        "[name=%s, size=%d, type=%s]",
                        file.getOriginalFilename(),
                        file.getSize(),
                        file.getContentType()
                    ))
                    .collect(Collectors.joining("; "));
                auditLog.setFileNames(fileNamesStr);
            }
            log.info(auditLog.toString());
            // Persist
            auditLogRepository.save(auditLog);

        } catch (Exception e) {
            // Log error but do not disrupt the main request flow
            e.printStackTrace();
        }
    }
}
