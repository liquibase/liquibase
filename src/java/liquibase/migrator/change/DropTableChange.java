package liquibase.migrator.change;

import liquibase.database.MSSQLDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.database.OracleDatabase;
import liquibase.database.PostgresDatabase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Drops an existing table.
 */
public class DropTableChange extends AbstractChange {

    private String tableName;
    private Boolean cascadeConstraints;

    public DropTableChange() {
        super("dropTable", "Drop Table");
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Boolean isCascadeConstraints() {
        return cascadeConstraints;
    }

    public void setCascadeConstraints(Boolean cascadeConstraints) {
        this.cascadeConstraints = cascadeConstraints;
    }

    private String[] generateStatements() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("DROP TABLE ").append(getTableName());
        if (isCascadeConstraints() != null && isCascadeConstraints()) {
            buffer.append(" CASCADE CONSTRAINTS");
        }
        return new String[]{buffer.toString()};
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
        return "Table " + tableName + " dropped";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement("dropTable");
        element.setAttribute("tableName", getTableName());

        if (isCascadeConstraints() != null) {
            element.setAttribute("cascadeConstraints", isCascadeConstraints().toString());
        }

        return element;
    }
}
