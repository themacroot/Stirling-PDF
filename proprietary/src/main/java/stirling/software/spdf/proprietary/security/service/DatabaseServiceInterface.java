package stirling.software.spdf.proprietary.security.service;

import java.sql.SQLException;
import java.util.List;

import stirling.software.spdf.proprietary.security.model.FileInfo;
import stirling.software.spdf.proprietary.security.model.exception.UnsupportedProviderException;

public interface DatabaseServiceInterface {
    void exportDatabase() throws SQLException, UnsupportedProviderException;

    void importDatabase();

    boolean hasBackup();

    List<FileInfo> getBackupList();
}
