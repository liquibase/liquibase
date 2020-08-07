package liquibase.harness.config;


public class TestInput {
    private String databaseName;
    private String url;
    private String dbSchema;
    private String username;
    private String password;
    private String version;
    private String changeObject;

    public TestInput(String databaseName, String url, String dbSchema, String username, String password, String version, String changeObject) {
        this.databaseName = databaseName;
        this.url = url;
        this.dbSchema = dbSchema;
        this.username = username;
        this.password = password;
        this.version = version;
        this.changeObject = changeObject;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDbSchema() {
        return dbSchema;
    }

    public void setDbSchema(String dbSchema) {
        this.dbSchema = dbSchema;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getChangeObject() {
        return changeObject;
    }

    public void setChangeObject(String changeObject) {
        this.changeObject = changeObject;
    }
}