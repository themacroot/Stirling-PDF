package stirling.software.SPDF.config.security;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.Map;

@Aspect
@Component
public class AuditAspect {

    @Autowired
    private AuditService auditService;

    @Before(
        "@annotation(RequestMapping) || @annotation(GetMapping) || @annotation(PostMapping) || " +
        "@annotation(PutMapping) || @annotation(DeleteMapping) || @annotation(PatchMapping)"
    )
    public void logApiCall(JoinPoint joinPoint) {
        try {
            // Grab the current request
            HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

            // Basic info
            String method = request.getMethod();
            String endpoint = request.getRequestURI();
            Map<String, String[]> requestParams = request.getParameterMap();
            String userAgent = request.getHeader("User-Agent");
            String ipAddress = getClientIp(request);
            String username = getCurrentUsername();

            // Extract file metadata if multipart
            MultipartFile[] files = extractFiles(request);

            // Pass to the audit service
            auditService.logRequest(
                method,
                endpoint,
                requestParams,
                userAgent,
                ipAddress,
                username,
                files
            );

        } catch (Exception e) {
            // Log and continue
            e.printStackTrace();
        }
    }

    private String getClientIp(HttpServletRequest request) {
        // Some networks use X-Forwarded-For to pass client IP
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    private String getCurrentUsername() {
        // If using Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return "anonymous";
    }

    private MultipartFile[] extractFiles(HttpServletRequest request) {
        if (request instanceof MultipartHttpServletRequest multipartRequest) {
            Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
            if (!fileMap.isEmpty()) {
                return fileMap.values().toArray(new MultipartFile[0]);
            }
        }
        return new MultipartFile[0];
    }
}
