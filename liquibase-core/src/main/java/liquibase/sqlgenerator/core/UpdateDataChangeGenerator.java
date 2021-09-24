package liquibase.sqlgenerator.core;

import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.ParameterizedSql;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedParameterizedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.UpdateExecutablePreparedStatement;

import java.util.ArrayList;
import java.util.List;

import static liquibase.util.SqlUtil.replacePredicatePlaceholders;

/**
 * Dummy SQL generator for <code>UpdateDataChange.ExecutableStatement</code><br>
 */
public class UpdateDataChangeGenerator extends AbstractSqlGenerator<UpdateExecutablePreparedStatement> {

    @Override
    public ValidationErrors validate(UpdateExecutablePreparedStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        if ((statement.getWhereParameters() != null) && !statement.getWhereParameters().isEmpty() && (statement
            .getWhereClause() == null)) {
            validationErrors.addError("whereParams set but no whereClause");
        }
        return validationErrors;
    }

    @Override
    public ParameterizedSql[] generateSql(UpdateExecutablePreparedStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        List<ColumnConfig> columns = new ArrayList<>(statement.getColumns().size());
        StringBuilder sql = new StringBuilder("UPDATE ").append(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()));

        StringBuilder params = new StringBuilder(" SET ");
        for(ColumnConfig column : statement.getColumns()) {
            params.append(database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), column.getName()));
            params.append(" = ");
            if (column.getValueObject() instanceof DatabaseFunction) {
                params.append(column.getValueObject()).append(", ");
            } else {
                params.append("?, ");
                columns.add(column);
            }
        }
        params.deleteCharAt(params.lastIndexOf(" "));
        params.deleteCharAt(params.lastIndexOf(","));
        sql.append(params);
        if (statement.getWhereClause() != null) {
            sql.append(" WHERE ").append(replacePredicatePlaceholders(database, statement.getWhereClause(), statement.getWhereColumnNames(), statement.getWhereParameters()));
        }

        return new ParameterizedSql[] {new UnparsedParameterizedSql(sql.toString(), columns)};
    }
}
