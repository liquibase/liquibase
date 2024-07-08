package liquibase.logging.mdc.customobjects;

import liquibase.change.core.SQLFileChange;
import liquibase.logging.mdc.CustomMdcObject;

/**
 * This class is used to represent MDC data related to a sqlFile type change inside a rollback.
 */
public class RollbackSqlFile implements CustomMdcObject {
    private String dbms;
    private String encoding;
    private String endDelimiter;
    private String path;
    private Boolean relativeToChangelogFile;
    private Boolean splitStatements;
    private Boolean stripComments;

    public RollbackSqlFile() {
    }

    public RollbackSqlFile(SQLFileChange change) {
        this.dbms = change.getDbms();
        this.endDelimiter = change.getEndDelimiter();
        this.splitStatements = change.isSplitStatements();
        this.stripComments = change.isStripComments();
        this.encoding = change.getEncoding();
        this.path = change.getPath();
        this.relativeToChangelogFile = change.isRelativeToChangelogFile();
    }

    public String getDbms() {
        return dbms;
    }

    public void setDbms(String dbms) {
        this.dbms = dbms;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getEndDelimiter() {
        return endDelimiter;
    }

    public void setEndDelimiter(String endDelimiter) {
        this.endDelimiter = endDelimiter;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean getRelativeToChangelogFile() {
        return relativeToChangelogFile;
    }

    public void setRelativeToChangelogFile(Boolean relativeToChangelogFile) {
        this.relativeToChangelogFile = relativeToChangelogFile;
    }

    public Boolean getSplitStatements() {
        return splitStatements;
    }

    public void setSplitStatements(Boolean splitStatements) {
        this.splitStatements = splitStatements;
    }

    public Boolean getStripComments() {
        return stripComments;
    }

    public void setStripComments(Boolean stripComments) {
        this.stripComments = stripComments;
    }
}
