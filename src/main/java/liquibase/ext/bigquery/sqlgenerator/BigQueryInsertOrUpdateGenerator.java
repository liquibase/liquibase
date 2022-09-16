package liquibase.ext.bigquery.sqlgenerator;


import java.util.Date;
import java.util.Iterator;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.ext.bigquery.database.BigqueryDatabase;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.InsertOrUpdateGenerator;
import liquibase.statement.core.InsertOrUpdateStatement;

public class BigQueryInsertOrUpdateGenerator extends InsertOrUpdateGenerator {
    public BigQueryInsertOrUpdateGenerator() {
    }

    public boolean supports(InsertOrUpdateStatement statement, Database database) {
        return database instanceof BigqueryDatabase;
    }

    protected String getInsertStatement(InsertOrUpdateStatement insertOrUpdateStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        Iterator var6 = insertOrUpdateStatement.getColumnValues().keySet().iterator();

        while(var6.hasNext()) {
            String columnKey = (String)var6.next();
            columns.append(",");
            columns.append(columnKey);
            values.append(",");
            values.append(this.convertToString(insertOrUpdateStatement.getColumnValue(columnKey), database));
        }

        columns.deleteCharAt(0);
        values.deleteCharAt(0);
        return "INSERT (" + columns + ") VALUES (" + values + ")";
    }

    protected String getUpdateStatement(InsertOrUpdateStatement insertOrUpdateStatement, Database database, String whereClause, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder sql = new StringBuilder("UPDATE SET ");
        Iterator var6 = insertOrUpdateStatement.getColumnValues().keySet().iterator();

        while(var6.hasNext()) {
            String columnKey = (String)var6.next();
            if (insertOrUpdateStatement.getAllowColumnUpdate(columnKey)) {
                sql.append(columnKey).append(" = ");
                sql.append(this.convertToString(insertOrUpdateStatement.getColumnValue(columnKey), database));
                sql.append(",");
            }
        }

        int lastComma = sql.lastIndexOf(",");
        if (lastComma > -1) {
            sql.deleteCharAt(lastComma);
        }

        return sql.toString();
    }

    protected String getRecordCheck(InsertOrUpdateStatement insertOrUpdateStatement, Database database, String whereClause) {
        return "MERGE INTO " + insertOrUpdateStatement.getTableName() + " USING (SELECT 1) ON " + whereClause + " WHEN NOT MATCHED THEN ";
    }

    protected String getElse(Database database) {
        return " WHEN MATCHED THEN ";
    }

    private String convertToString(Object newValue, Database database) {
        String sqlString;
        if (newValue != null && !"".equals(newValue.toString()) && !"NULL".equalsIgnoreCase(newValue.toString())) {
            if (newValue instanceof String && !this.looksLikeFunctionCall((String)newValue, database)) {
                sqlString = "'" + database.escapeStringForDatabase(newValue.toString()) + "'";
            } else if (newValue instanceof Date) {
                sqlString = database.getDateLiteral((Date)newValue);
            } else if (newValue instanceof Boolean) {
                if (Boolean.TRUE.equals(newValue)) {
                    sqlString = DataTypeFactory.getInstance().getTrueBooleanValue(database);
                } else {
                    sqlString = DataTypeFactory.getInstance().getFalseBooleanValue(database);
                }
            } else {
                sqlString = newValue.toString();
            }
        } else {
            sqlString = "NULL";
        }

        return sqlString;
    }
}
