package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.TextNode;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DeleteStatement;
import liquibase.util.StringUtils;

public class DeleteDataChange extends AbstractChange {

    private String schemaName;
    private String tableName;

    @TextNode(nodeName="where")
    private String whereClause;

    public DeleteDataChange() {
        super("delete", "Delete Data", ChangeMetaData.PRIORITY_DEFAULT);
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getWhereClause() {
        return whereClause;
    }

    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }

    public SqlStatement[] generateStatements(Database database) {

        DeleteStatement statement = new DeleteStatement(getSchemaName() == null ? database.getDefaultSchemaName() : getSchemaName(), getTableName());

        statement.setWhereClause(whereClause);

        return new SqlStatement[]{
                statement
        };
    }

    public String getConfirmationMessage() {
        return "Data deleted from " + getTableName();
    }

}
