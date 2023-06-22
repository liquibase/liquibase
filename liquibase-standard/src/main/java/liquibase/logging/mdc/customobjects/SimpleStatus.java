package liquibase.logging.mdc.customobjects;

import liquibase.changelog.ChangeSet;
import liquibase.logging.mdc.CustomMdcObject;

import java.util.List;

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLiquibaseTargetUrl() {
        return liquibaseTargetUrl;
    }

    public void setLiquibaseTargetUrl(String liquibaseTargetUrl) {
        this.liquibaseTargetUrl = liquibaseTargetUrl;
    }

    public int getChangesetCount() {
        return changesetCount;
    }

    public void setChangesetCount(int changesetCount) {
        this.changesetCount = changesetCount;
    }
}
