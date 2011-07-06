package liquibase.sqlgenerator.core;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.InsertOrUpdateStatement;

/**
 *
 * @author Carles
 */
public class InsertOrUpdateGeneratorMySQL extends InsertOrUpdateGenerator {
    @Override
    public boolean supports(InsertOrUpdateStatement statement, Database database) {
         return database instanceof MySQLDatabase;
    }

    @Override
    protected String getInsertStatement(InsertOrUpdateStatement insertOrUpdateStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuffer sql = new StringBuffer(super.getInsertStatement(insertOrUpdateStatement, database, sqlGeneratorChain));
        
        sql.deleteCharAt(sql.lastIndexOf(";"));
        sql.append("ON DUPLICATE KEY UPDATE ");
        String[] pkFields=insertOrUpdateStatement.getPrimaryKey().split(",");
        HashSet<String> hashPkFields = new HashSet<String>(Arrays.asList(pkFields));
        for(String columnKey:insertOrUpdateStatement.getColumnValues().keySet())
        {
            if (!hashPkFields.contains(columnKey)) {
                sql.append(columnKey).append(" = ");
                sql.append(convertToString(insertOrUpdateStatement.getColumnValue(columnKey),database));
                sql.append(",");
            }
        }
        sql.deleteCharAt(sql.lastIndexOf(","));

        return sql.toString();
    }

    @Override
    protected String getUpdateStatement(InsertOrUpdateStatement insertOrUpdateStatement, Database database, String whereClause, SqlGeneratorChain sqlGeneratorChain) {
        return "";
    }

    @Override
    protected String getRecordCheck(InsertOrUpdateStatement insertOrUpdateStatement, Database database, String whereClause) {
        return "";
    }

    @Override
    protected String getElse(Database database) {
        return "";
    }

    private String convertToString(Object newValue, Database database) {
        String sqlString;
        if (newValue == null || newValue.toString().equals("") || newValue.toString().equalsIgnoreCase("NULL")) {
            sqlString = "NULL";
        } else if (newValue instanceof String && database.shouldQuoteValue(((String) newValue))) {
            sqlString = "'" + database.escapeStringForDatabase(newValue.toString()) + "'";
        } else if (newValue instanceof Date) {
            sqlString = database.getDateLiteral(((Date) newValue));
        } else if (newValue instanceof Boolean) {
            if (((Boolean) newValue)) {
                sqlString = TypeConverterFactory.getInstance().findTypeConverter(database).getBooleanType().getTrueBooleanValue();
            } else {
                sqlString = TypeConverterFactory.getInstance().findTypeConverter(database).getBooleanType().getFalseBooleanValue();
            }
        } else {
            sqlString = newValue.toString();
        }
        return sqlString;
    }
}
