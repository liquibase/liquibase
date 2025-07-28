package liquibase.change.core.snowflake;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.snowflake.DropSchemaStatement;

/**
 * Drops a Snowflake schema.
 */
@DatabaseChange(name = "dropSchema", 
    description = "Drops a Snowflake schema", 
    priority = ChangeMetaData.PRIORITY_DEFAULT)
public class DropSchemaChange extends AbstractChange {

    private String databaseName;
    private String schemaName;
    private Boolean ifExists;
    private Boolean cascade;

    @DatabaseChangeProperty(description = "Name of the database")
    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @DatabaseChangeProperty(description = "Name of the schema to drop", requiredForDatabase = "snowflake")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(description = "Whether to add IF EXISTS to the SQL statement")
    public Boolean getIfExists() {
        return ifExists;
    }

    public void setIfExists(Boolean ifExists) {
        this.ifExists = ifExists;
    }

    @DatabaseChangeProperty(description = "Whether to add CASCADE to the SQL statement")
    public Boolean getCascade() {
        return cascade;
    }

    public void setCascade(Boolean cascade) {
        this.cascade = cascade;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
            new DropSchemaStatement(getDatabaseName(), getSchemaName(), getIfExists(), getCascade())
        };
    }

    @Override
    public String getConfirmationMessage() {
        return "Schema " + getSchemaName() + " dropped";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return "http://www.liquibase.org/xml/ns/snowflake";
    }
}