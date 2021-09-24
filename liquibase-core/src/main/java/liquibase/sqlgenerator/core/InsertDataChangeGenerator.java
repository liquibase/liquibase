package liquibase.sqlgenerator.core;

import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.ParameterizedSql;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedParameterizedSql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.InsertExecutablePreparedStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * Dummy SQL generator for <code>InsertDataChange.ExecutableStatement</code><br>
 */
public class InsertDataChangeGenerator extends AbstractSqlGenerator<InsertExecutablePreparedStatement> {

    @Override
    public ValidationErrors validate(InsertExecutablePreparedStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public ParameterizedSql[] generateSql(InsertExecutablePreparedStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        List<ColumnConfig> columns = new ArrayList<>(statement.getColumns().size());
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        StringBuilder params = new StringBuilder("VALUES(");
        sql.append(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()));
        sql.append("(");
        for(ColumnConfig column : statement.getColumns()) {
            if(database.supportsAutoIncrement()
                    && Boolean.TRUE.equals(column.isAutoIncrement())) {
                continue;
            }
            sql.append(database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), column.getName()));
            sql.append(", ");
            if (column.getValueObject() instanceof DatabaseFunction) {
                params.append(column.getValueObject()).append(", ");
            } else {
                params.append("?, ");
                columns.add(column);
            }
        }
        sql.deleteCharAt(sql.lastIndexOf(" "));
        sql.deleteCharAt(sql.lastIndexOf(","));
        params.deleteCharAt(params.lastIndexOf(" "));
        params.deleteCharAt(params.lastIndexOf(","));
        params.append(")");
        sql.append(") ");
        sql.append(params);
        return new ParameterizedSql[] {new UnparsedParameterizedSql(sql.toString(), columns)};

    }
}
