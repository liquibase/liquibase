package liquibase.logging.mdc.customobjects;

import liquibase.changelog.ChangeSet;
import liquibase.logging.mdc.CustomMdcObject;
import lombok.Getter;

import java.util.List;

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

    public void setMessage(String message) {
        this.message = message;
    }

    public void setLiquibaseTargetUrl(String liquibaseTargetUrl) {
        this.liquibaseTargetUrl = liquibaseTargetUrl;
    }

    public void setChangesetCount(int changesetCount) {
        this.changesetCount = changesetCount;
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
