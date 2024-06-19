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
    private String procedureArguments;
    private String dropName;

    @DatabaseChangeProperty(mustEqualExisting ="storedProcedure.catalog")
    public String getCatalogName() {
        return this.catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="storedProcedure.schema")
    public String getSchemaName() {
        return this.schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "storedProcedure", description = "Name of the stored procedure to drop", exampleValue = "new_customer")
    public String getProcedureName() {
        return this.procedureName;
    }

    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
    }

    @Override
    public String getConfirmationMessage() {
        return "Stored Procedure " + getProcedureName() + " dropped";
    }

    @DatabaseChangeProperty(mustEqualExisting = "storedProcedure.dropName", description = "Name of the procedure to drop if function is overloaded")
    public String getDropName() {
        return this.dropName;
    }

    public void setDropName(String dropName) {
        this.dropName = dropName;
    }
    @DatabaseChangeProperty(mustEqualExisting = "storedProcedure.arguments", description = "Arguments of the procedure if it is overloaded")
    public String getProcedureArguments() {
        return this.procedureArguments;
    }

    public void setProcedureArguments(String args) {
        this.procedureArguments = args;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        DropProcedureStatement statement = new DropProcedureStatement(getCatalogName(), getSchemaName(),
                getProcedureName(), getDropName(), getProcedureArguments());
        return new SqlStatement[]{statement};
    }

    @Override
    public String getSerializedObjectNamespace() {
        return LiquibaseSerializable.STANDARD_CHANGELOG_NAMESPACE;
    }
}
