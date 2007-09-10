package liquibase.change;

import liquibase.database.Database;
import liquibase.database.sql.SqlStatement;
import liquibase.database.sql.RawSqlStatement;
import liquibase.database.structure.DatabaseObject;
import liquibase.exception.UnsupportedChangeException;
import liquibase.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;

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

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        for (String sql : StringUtils.processMutliLineSQL(this.sql)) {
            statements.add(new RawSqlStatement(sql));
        }

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    public String getConfirmationMessage() {
        return "Custom SQL has been executed";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element sqlElement = currentChangeLogFileDOM.createElement("sql");
        sqlElement.appendChild(currentChangeLogFileDOM.createTextNode(getSql()));

        return sqlElement;
    }


    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        return null;
    }
}
