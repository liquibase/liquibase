package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropProcedureStatement;
import lombok.Setter;

@DatabaseChange(name = "dropProcedure", description = "Drops an existing procedure", priority = ChangeMetaData.PRIORITY_DEFAULT+100,
    appliesTo = "storedProcedure")
@Setter
public class DropProcedureChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String procedureName;

    @DatabaseChangeProperty(mustEqualExisting ="storedProcedure.catalog", description = "Name of the database catalog")
    public String getCatalogName() {
        return catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="storedProcedure.schema", description = "Name of the database schema")
    public String getSchemaName() {
        return schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "storedProcedure", description = "Name of the stored procedure to drop",
        exampleValue = "new_customer")
    public String getProcedureName() {
        return procedureName;
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
