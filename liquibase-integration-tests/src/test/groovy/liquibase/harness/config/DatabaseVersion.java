package liquibase.harness.config;

import java.util.List;

public class DatabaseVersion {
    private String version;
    private String url;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getChangeObjects() {
        return changeObjects;
    }

    public void setChangeObjects(List<String> changeObjects) {
        this.changeObjects = changeObjects;
    }

    private List<String> changeObjects;
}
