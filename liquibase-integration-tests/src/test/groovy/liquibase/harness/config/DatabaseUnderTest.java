package liquibase.harness.config;

import java.util.List;

public class DatabaseUnderTest {
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<DatabaseVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<DatabaseVersion> versions) {
        this.versions = versions;
    }

    private List<DatabaseVersion> versions;
}
