package stirling.software.spdf.proprietary.security.persistence;

import java.sql.SQLException;

import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import stirling.software.spdf.proprietary.security.controller.api.H2SQLCondition;
import stirling.software.spdf.proprietary.security.model.exception.UnsupportedProviderException;
import stirling.software.spdf.proprietary.security.service.DatabaseServiceInterface;

@Component
@Conditional(H2SQLCondition.class)
public class ScheduledTasks {

    private final DatabaseServiceInterface databaseService;

    public ScheduledTasks(DatabaseServiceInterface databaseService) {
        this.databaseService = databaseService;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void performBackup() throws SQLException, UnsupportedProviderException {
        databaseService.exportDatabase();
    }
}
