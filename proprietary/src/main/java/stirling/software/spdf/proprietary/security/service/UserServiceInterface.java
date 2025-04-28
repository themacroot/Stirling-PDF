package stirling.software.spdf.proprietary.security.service;

public interface UserServiceInterface {
    String getApiKeyForUser(String username);

    String getCurrentUsername();

    long getTotalUsersCount();
}
