package liquibase.sqlgenerator.core;

import java.util.Arrays;
import java.util.HashSet;
import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.datatype.DataTypeFactory;
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
        
        StringBuffer updateClause = new StringBuffer("ON DUPLICATE KEY UPDATE ");
        String[] pkFields=insertOrUpdateStatement.getPrimaryKey().split(",");
        HashSet<String> hashPkFields = new HashSet<String>(Arrays.asList(pkFields));
        boolean hasFields = false;
        for(String columnKey:insertOrUpdateStatement.getColumnValues().keySet())
        {
            if (!hashPkFields.contains(columnKey)) {
            	hasFields = true;
            	updateClause.append(columnKey).append(" = ");
                Object columnValue = insertOrUpdateStatement.getColumnValue(columnKey);
                updateClause.append(DataTypeFactory.getInstance().fromObject(columnValue, database).objectToSql(columnValue, database));
            	updateClause.append(",");
            }
        }
        
        if(hasFields) {
        	// append the updateClause onto the end of the insert statement
            updateClause.deleteCharAt(updateClause.lastIndexOf(","));
        	sql.append(updateClause);
        } else {
        	// insert IGNORE keyword into insert statement
        	sql.insert(sql.indexOf("INSERT ")+"INSERT ".length(), "IGNORE ");
        }

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
}
