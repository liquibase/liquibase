package liquibase.change.core;

import liquibase.change.*;
import  liquibase.ExecutionEnvironment;
import liquibase.snapshot.SnapshotFactory;
import liquibase.statement.Statement;
import liquibase.statement.core.DropTableStatement;
import liquibase.structure.core.Table;

/**
 * Drops an existing table.
 */
@DatabaseChange(name="dropTable", description = "Drops an existing table", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
public class DropTableChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private Boolean cascadeConstraints;

    @DatabaseChangeProperty(mustEqualExisting ="table.catalog", since = "3.0")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="table.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "table", description = "Name of the table to drop")
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

    @Override
    public Statement[] generateStatements(ExecutionEnvironment env) {
        boolean constraints = false;
        if (isCascadeConstraints() != null) {
            constraints = isCascadeConstraints();
        }
        
        return new Statement[]{
                new DropTableStatement(getCatalogName(), getSchemaName(), getTableName(), constraints)
        };
    }

    @Override
    public ChangeStatus checkStatus(ExecutionEnvironment env) {
        try {
            return new ChangeStatus().assertComplete(!SnapshotFactory.getInstance().has(new Table(getCatalogName(), getSchemaName(), getTableName()), env.getTargetDatabase()), "Table exists");
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }
    }


    @Override
    public String getConfirmationMessage() {
        return "Table " + getTableName() + " dropped";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
