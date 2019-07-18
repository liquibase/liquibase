package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropProcedureStatement;

@DatabaseChange(name="dropProcedure", description = "Drops an existing procedure", priority = ChangeMetaData.PRIORITY_DEFAULT+100, appliesTo = "storedProcedure")
public class DropProcedureChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String procedureName;

    @DatabaseChangeProperty(mustEqualExisting ="storedProcedure.catalog")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="storedProcedure.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "storedProcedure", description = "Name of the stored procedure to drop", exampleValue = "new_customer")
    public String getProcedureName() {
        return procedureName;
    }

    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
    }

    @Override
    public String getConfirmationMessage() {
        return "Stored Procedure "+getProcedureName()+" dropped";
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
                new DropProcedureStatement(getCatalogName(), getSchemaName(), getProcedureName())
        };
    }

    @Override
    public String getSerializedObjectNamespace() {
        return LiquibaseSerializable.STANDARD_CHANGELOG_NAMESPACE;
    }
}
