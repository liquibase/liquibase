package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import lombok.Getter;
import lombok.Setter;

public class SetNullableStatement extends AbstractSqlStatement {
    @Getter
    private final String columnName;
    @Getter
    private final String columnDataType;
    @Getter
    private final boolean nullable;
    @Getter
    private String constraintName;
    @Setter
    @Getter
    private boolean validate = true;
    private DatabaseTableIdentifier databaseTableIdentifier = new DatabaseTableIdentifier(null, null, null);

    public SetNullableStatement(String catalogName, String schemaName, String tableName, String columnName, String columnDataType, boolean nullable) {
        this.databaseTableIdentifier.setCatalogName(catalogName);
        this.databaseTableIdentifier.setSchemaName(schemaName);
        this.databaseTableIdentifier.setTableName(tableName);
        this.columnName = columnName;
        this.columnDataType = columnDataType;
        this.nullable = nullable;
    }

    public SetNullableStatement(String catalogName, String schemaName, String tableName, String columnName, String columnDataType, boolean nullable, String constraintName) {
        this(catalogName, schemaName, tableName, columnName, columnDataType, nullable);
        this.constraintName = constraintName;
    }

    public SetNullableStatement(String catalogName, String schemaName, String tableName, String columnName,
                                String columnDataType, boolean nullable, String constraintName, Boolean validate) {
        this(catalogName, schemaName, tableName, columnName, columnDataType, nullable);
        this.constraintName = constraintName;
        if (validate!=null) {
            this.validate = validate;
        }
    }

    public String getCatalogName() {
        return databaseTableIdentifier.getCatalogName();
    }

    public String getSchemaName() {
        return databaseTableIdentifier.getSchemaName();
    }

    public String getTableName() {
        return databaseTableIdentifier.getTableName();
    }

}
