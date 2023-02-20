package liquibase.logging.mdc.customobjects;

import liquibase.change.AbstractSQLChange;
import liquibase.change.core.SQLFileChange;
import liquibase.logging.mdc.CustomMdcObject;

public class RollbackSqlFile implements CustomMdcObject {
    private String dbms;
    private String encoding;
    private String endDelimiter;
    private String path;
    private boolean relativeToChangelogFile;
    private boolean splitStatements;
    private boolean stripComments;

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

    public boolean isRelativeToChangelogFile() {
        return relativeToChangelogFile;
    }

    public void setRelativeToChangelogFile(boolean relativeToChangelogFile) {
        this.relativeToChangelogFile = relativeToChangelogFile;
    }

    public boolean isSplitStatements() {
        return splitStatements;
    }

    public void setSplitStatements(boolean splitStatements) {
        this.splitStatements = splitStatements;
    }

    public boolean isStripComments() {
        return stripComments;
    }

    public void setStripComments(boolean stripComments) {
        this.stripComments = stripComments;
    }
}
