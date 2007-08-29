package liquibase.migrator.change;

import liquibase.database.Database;
import liquibase.migrator.exception.UnsupportedChangeException;
import liquibase.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Allows execution of arbitrary SQL.  This change can be used when existing changes are either don't exist,
 * are not flexible enough, or buggy. 
 */
public class RawSQLChange extends AbstractChange {

    private String comments;
    private String sql;

    public RawSQLChange() {
        super("sql", "Custom SQL");
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String[] generateStatements(Database database) throws UnsupportedChangeException {
        return StringUtils.processMutliLineSQL(sql);
    }

    public String getConfirmationMessage() {
        return "Custom SQL has been executed";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element sqlElement = currentChangeLogFileDOM.createElement("sql");
        sqlElement.appendChild(currentChangeLogFileDOM.createTextNode(getSql()));

        return sqlElement;
    }
}
