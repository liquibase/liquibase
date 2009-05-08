package liquibase.change;

import liquibase.database.Database;
import liquibase.statement.DropTableStatement;
import liquibase.statement.SqlStatement;
import liquibase.util.StringUtils;

/**
 * Drops an existing table.
 */
public class DropTableChange extends AbstractChange {

    private String schemaName;
    private String tableName;
    private Boolean cascadeConstraints;

    public DropTableChange() {
        super("dropTable", "Drop Table", ChangeMetaData.PRIORITY_DEFAULT);
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

    public Boolean isCascadeConstraints() {
        return cascadeConstraints;
    }

    public void setCascadeConstraints(Boolean cascadeConstraints) {
        this.cascadeConstraints = cascadeConstraints;
    }

    public SqlStatement[] generateStatements(Database database) {
        boolean constraints = false;
        if (isCascadeConstraints() != null) {
            constraints = isCascadeConstraints();
        }
        
        return new SqlStatement[]{
                new DropTableStatement(getSchemaName() == null?database.getDefaultSchemaName():getSchemaName(), getTableName(), constraints)
        };
    }

    public String getConfirmationMessage() {
        return "Table " + getTableName() + " dropped";
    }

}
