package liquibase.database.sql;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.database.MSSQLDatabase;
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

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
        StringBuffer buffer = new StringBuffer();

        buffer.append("CREATE ");
        if (unique != null && unique) {
            buffer.append("UNIQUE ");
        }            
        buffer.append("INDEX ");

        buffer.append(database.escapeIndexName(null, getIndexName())).append(" ON ");
        buffer.append(database.escapeTableName(getTableSchemaName(), getTableName())).append("(");
        Iterator<String> iterator = Arrays.asList(getColumns()).iterator();
        while (iterator.hasNext()) {
            String column = iterator.next();
            buffer.append(database.escapeColumnName(getTableSchemaName(), getTableName(), column));
            if (iterator.hasNext()) {
                buffer.append(", ");
            }
        }
        buffer.append(")");

        if (StringUtils.trimToNull(getTablespace()) != null && database.supportsTablespaces()) {
            if (database instanceof MSSQLDatabase) {
                buffer.append(" ON ").append(getTablespace());
            } else if (database instanceof DB2Database) {
                buffer.append(" IN ").append(getTablespace());
            } else {
                buffer.append(" TABLESPACE ").append(getTablespace());
            }
        }

        return buffer.toString();
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
        return true;
    }
}
