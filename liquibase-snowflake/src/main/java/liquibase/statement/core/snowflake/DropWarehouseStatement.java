package liquibase.statement.core.snowflake;

import liquibase.statement.AbstractSqlStatement;

public class DropWarehouseStatement extends AbstractSqlStatement {
    
    private String warehouseName;
    private Boolean ifExists;

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public Boolean getIfExists() {
        return ifExists;
    }

    public void setIfExists(Boolean ifExists) {
        this.ifExists = ifExists;
    }
}