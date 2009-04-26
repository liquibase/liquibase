package liquibase.database.statement;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.database.MSSQLDatabase;
import liquibase.database.SybaseASADatabase;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.util.StringUtils;

import java.util.Arrays;
import java.util.Iterator;

public class CreateIndexStatement implements SqlStatement {

    private String tableSchemaName;
    private String indexName;
    private String tableName;
    private String[] columns;
    private String tablespace;
    private Boolean unique;

    public CreateIndexStatement(String indexName, String tableSchemaName, String tableName, Boolean isUnique, String... columns) {
        this.indexName = indexName;
        this.tableSchemaName = tableSchemaName;
        this.tableName = tableName;
        this.columns = columns;
        this.unique = isUnique;
    }

    public String getTableSchemaName() {
        return tableSchemaName;
    }

    public String getIndexName() {
        return indexName;
    }

    public String getTableName() {
        return tableName;
    }

    public String[] getColumns() {
        return columns;
    }

    public String getTablespace() {
        return tablespace;
    }

    public CreateIndexStatement setTablespace(String tablespace) {
        this.tablespace = tablespace;

        return this;
    }

    public Boolean isUnique() {
        return unique;
    }
}
