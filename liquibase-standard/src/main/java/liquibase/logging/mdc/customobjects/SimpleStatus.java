package liquibase.logging.mdc.customobjects;

import liquibase.changelog.ChangeSet;
import liquibase.logging.mdc.CustomMdcObject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class SimpleStatus implements CustomMdcObject {

    private String message;
    private String liquibaseTargetUrl;
    private int changesetCount;

    public SimpleStatus() {
    }

    public SimpleStatus(String message, String url, List<ChangeSet> unrunChangeSets) {
        this.message = message;
        this.liquibaseTargetUrl = url;
        this.changesetCount = unrunChangeSets.size();
    }

    @Override
    public String toString() {
        return "SimpleStatus{" +
                "message='" + message + '\'' +
                ", liquibaseTargetUrl='" + liquibaseTargetUrl + '\'' +
                ", changesetCount=" + changesetCount +
                '}';
    }
}
