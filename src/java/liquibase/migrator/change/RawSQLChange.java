package liquibase.migrator.change;

import liquibase.database.*;
import liquibase.migrator.UnsupportedChangeException;
import liquibase.migrator.RollbackImpossibleException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RawSQLChange extends AbstractChange {

    private String comment;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    private String[] generateStatements() {
        return new String[] { sql.replaceFirst(";$", "") };
    }

    public String[] generateStatements(MSSQLDatabase database) {
        return generateStatements();
    }

    public String[] generateStatements(OracleDatabase database) {
        return generateStatements();
    }

    public String[] generateStatements(MySQLDatabase database) {
        return generateStatements();
    }

    public String[] generateStatements(PostgresDatabase database) {
        return generateStatements();
    }

    public String getConfirmationMessage() {
        return "Custom SQL has been executed";
    }

    public Element createNode(Document currentMigrationFileDOM) {
        Element sqlElement = currentMigrationFileDOM.createElement("sql");
        sqlElement.appendChild(currentMigrationFileDOM.createTextNode(getSql()));

        return sqlElement;
    }
}
