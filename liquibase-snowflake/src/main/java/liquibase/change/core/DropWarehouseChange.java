package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.snowflake.DropWarehouseStatement;

/**
 * Drops a warehouse in Snowflake.
 */
@DatabaseChange(
    name = "dropWarehouse",
    description = "Drops a warehouse",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "warehouse",
    since = "4.33"
)
public class DropWarehouseChange extends AbstractChange {

    private String warehouseName;
    private Boolean ifExists;

    @DatabaseChangeProperty(description = "Name of the warehouse to drop", requiredForDatabase = "snowflake")
    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    @DatabaseChangeProperty(description = "Only drop the warehouse if it exists")
    public Boolean getIfExists() {
        return ifExists;
    }

    public void setIfExists(Boolean ifExists) {
        this.ifExists = ifExists;
    }

    @Override
    public boolean supports(Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors errors = super.validate(database);
        
        if (warehouseName == null || warehouseName.trim().isEmpty()) {
            errors.addError("warehouseName is required");
        }
        
        return errors;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        DropWarehouseStatement statement = new DropWarehouseStatement();
        statement.setWarehouseName(getWarehouseName());
        statement.setIfExists(getIfExists());
        
        return new SqlStatement[]{statement};
    }

    @Override
    public String getConfirmationMessage() {
        return "Warehouse " + getWarehouseName() + " dropped";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return "http://www.liquibase.org/xml/ns/snowflake";
    }
}