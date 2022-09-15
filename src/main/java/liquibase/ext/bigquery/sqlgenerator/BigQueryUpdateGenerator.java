package liquibase.ext.bigquery.sqlgenerator;

import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.UpdateGenerator;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.core.UpdateStatement;
import liquibase.structure.DatabaseObject;
import liquibase.util.SqlUtil;

import java.util.Date;
import java.util.Iterator;

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
        StringBuilder sql = (new StringBuilder("UPDATE ")).append(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())).append(" SET");
        Iterator var5 = statement.getNewColumnValues().keySet().iterator();

        while(var5.hasNext()) {
            String column = (String)var5.next();
            sql.append(" ").append(database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), column)).append(" = ").append(this.convertToString(statement.getNewColumnValues().get(column), database)).append(",");
        }

        int lastComma = sql.lastIndexOf(",");
        if (lastComma >= 0) {
            sql.deleteCharAt(lastComma);
        }

        if (statement.getWhereClause() != null) {
            sql.append(" WHERE ").append(SqlUtil.replacePredicatePlaceholders(database, statement.getWhereClause(), statement.getWhereColumnNames(), statement.getWhereParameters()));
        }else{
            sql.append(" WHERE 1=1");
        }

        return new Sql[]{new UnparsedSql(sql.toString(), new DatabaseObject[]{this.getAffectedTable(statement)})};
    }

    private String convertToString(Object newValue, Database database) {
        String sqlString;
        if (newValue != null && !"NULL".equalsIgnoreCase(newValue.toString())) {
            if (newValue instanceof String && !this.looksLikeFunctionCall((String)newValue, database)) {
                sqlString = DataTypeFactory.getInstance().fromObject(newValue, database).objectToSql(newValue, database);
            } else if (newValue instanceof Date) {
                Date date = (Date)newValue;
                if (date.getClass().equals(Date.class)) {
                    date = new java.sql.Date(((Date)date).getTime());
                }

                sqlString = database.getDateLiteral((Date)date);
            } else if (newValue instanceof Boolean) {
                if ((Boolean)newValue) {
                    sqlString = DataTypeFactory.getInstance().getTrueBooleanValue(database);
                } else {
                    sqlString = DataTypeFactory.getInstance().getFalseBooleanValue(database);
                }
            } else if (newValue instanceof DatabaseFunction) {
                sqlString = database.generateDatabaseFunctionValue((DatabaseFunction)newValue);
            } else {
                sqlString = newValue.toString();
            }
        } else {
            sqlString = "NULL";
        }

        return sqlString;
    }

}
