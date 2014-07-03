package liquibase.sqlgenerator.core;

import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.datatype.DataTypeFactory;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.InsertOrUpdateDataStatement;

import java.util.Arrays;
import java.util.HashSet;

/**
 *
 * @author Carles
 */
public class InsertOrUpdateGeneratorMySQL extends InsertOrUpdateGenerator {
    @Override
    public boolean supports(InsertOrUpdateDataStatement statement, ExecutionEnvironment env) {
        Database database = env.getTargetDatabase();

        return database instanceof MySQLDatabase;
    }

    @Override
    protected String getInsertStatement(InsertOrUpdateDataStatement insertOrUpdateDataStatement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        Database database = env.getTargetDatabase();

        StringBuffer sql = new StringBuffer(super.getInsertStatement(insertOrUpdateDataStatement, env, chain));
        
        sql.deleteCharAt(sql.lastIndexOf(";"));
        
        StringBuffer updateClause = new StringBuffer("ON DUPLICATE KEY UPDATE ");
        String[] pkFields= insertOrUpdateDataStatement.getPrimaryKey().split(",");
        HashSet<String> hashPkFields = new HashSet<String>(Arrays.asList(pkFields));
        boolean hasFields = false;
        for(String columnKey: insertOrUpdateDataStatement.getColumnNames())
        {
            if (!hashPkFields.contains(columnKey)) {
            	hasFields = true;
            	updateClause.append(columnKey).append(" = ");
                Object columnValue = insertOrUpdateDataStatement.getColumnValue(columnKey);
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
    protected String getUpdateStatement(InsertOrUpdateDataStatement insertOrUpdateDataStatement, ExecutionEnvironment env, String whereClause, StatementLogicChain chain) {
        return "";
    }

    @Override
    protected String getRecordCheck(InsertOrUpdateDataStatement insertOrUpdateDataStatement, ExecutionEnvironment env, String whereClause) {
        return "";
    }

    @Override
    protected String getElse(ExecutionEnvironment env) {
        return "";
    }
}
