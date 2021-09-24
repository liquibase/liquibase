package liquibase.sql;

import liquibase.change.ColumnConfig;

import java.util.List;

public interface ParameterizedSql extends Sql {

    List<ColumnConfig> getColumns();
}
