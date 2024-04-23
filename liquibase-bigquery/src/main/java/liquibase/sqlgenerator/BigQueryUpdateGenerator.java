package liquibase.sqlgenerator;

import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.UpdateGenerator;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.core.UpdateStatement;
import liquibase.util.SqlUtil;

import java.util.Date;

public class BigQueryUpdateGenerator extends UpdateGenerator {

    public BigQueryUpdateGenerator() {
        super();
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }


    @Override
    public Sql[] generateSql(UpdateStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder sql = new StringBuilder("UPDATE ")
                .append(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()))
                .append(" SET");

        for (String column : statement.getNewColumnValues().keySet()) {
            sql.append(" ")
                    .append(database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), column))
                    .append(" = ")
                    .append(this.convertToString(statement.getNewColumnValues().get(column), database))
                    .append(",");
        }

        int lastComma = sql.lastIndexOf(",");
        if (lastComma >= 0) {
            sql.deleteCharAt(lastComma);
        }

        if (statement.getWhereClause() != null) {
            sql.append(" WHERE ").append(SqlUtil.replacePredicatePlaceholders(database, statement.getWhereClause(), statement.getWhereColumnNames(), statement.getWhereParameters()));
        } else {
            sql.append(" WHERE 1 = 1");
        }

        return new Sql[]{
                new UnparsedSql(sql.toString(), getAffectedTable(statement))
        };
    }

    private String convertToString(Object newValue, Database database) {
        String sqlString;
        if ((newValue == null) || "NULL".equalsIgnoreCase(newValue.toString())) {
            sqlString = "NULL";
        } else if ((newValue instanceof String) && !looksLikeFunctionCall(((String) newValue), database)) {
            sqlString = DataTypeFactory.getInstance().fromObject(newValue, database).objectToSql(newValue, database);
        } else if (newValue instanceof Date) {
            // converting java.util.Date to java.sql.Date
            Date date = (Date) newValue;
            if (date.getClass().equals(java.util.Date.class)) {
                date = new java.sql.Date(date.getTime());
            }

            sqlString = database.getDateLiteral(date);
        } else if (newValue instanceof Boolean) {
            if (((Boolean) newValue)) {
                sqlString = DataTypeFactory.getInstance().getTrueBooleanValue(database);
            } else {
                sqlString = DataTypeFactory.getInstance().getFalseBooleanValue(database);
            }
        } else if (newValue instanceof DatabaseFunction) {
            sqlString = database.generateDatabaseFunctionValue((DatabaseFunction) newValue);
        } else {
            sqlString = newValue.toString();
        }
        return sqlString;
    }

}
