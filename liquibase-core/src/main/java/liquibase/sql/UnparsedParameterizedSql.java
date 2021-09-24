package liquibase.sql;

import liquibase.change.ColumnConfig;
import liquibase.structure.DatabaseObject;

import java.util.List;

public class UnparsedParameterizedSql extends UnparsedSql implements ParameterizedSql {

    private final List<ColumnConfig> columns;

    public UnparsedParameterizedSql(String sql, List<ColumnConfig> columns, DatabaseObject... affectedDatabaseObjects) {
        super(sql, affectedDatabaseObjects);
        this.columns = columns;
    }

    @Override
    public List<ColumnConfig> getColumns() {
        return columns;
    }
}
