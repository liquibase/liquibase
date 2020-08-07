package liquibase.harness.config;

import java.util.List;

public class DatabaseUnderTest {
    private String name;
    private String username;
    private String password;
    private List<DatabaseVersion> versions;
    private List<String> changeObjects;
    private String dbSchema;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
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

    public List<String> getChangeObjects() {
        return changeObjects;
    }

    public void setChangeObjects(List<String> changeObjects) {
        this.changeObjects = changeObjects;
    }

    public String getDbSchema() {
        return dbSchema;
    }

    public void setDbSchema(String dbSchema) {
        this.dbSchema = dbSchema;
    }
}
