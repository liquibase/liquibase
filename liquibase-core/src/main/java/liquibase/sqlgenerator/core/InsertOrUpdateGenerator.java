package liquibase.sqlgenerator.core;

import liquibase.ExecutionEnvironment;
import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.LiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.statement.core.InsertOrUpdateStatement;
import liquibase.statement.core.UpdateStatement;

import java.util.Arrays;
import java.util.HashSet;

public abstract class InsertOrUpdateGenerator extends AbstractSqlGenerator<InsertOrUpdateStatement> {

    protected abstract String getRecordCheck(InsertOrUpdateStatement insertOrUpdateStatement, ExecutionEnvironment env, String whereClause);

    protected abstract String getElse(ExecutionEnvironment env);

    protected String getPostUpdateStatements(ExecutionEnvironment env){
        return "";
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public ValidationErrors validate(InsertOrUpdateStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", statement.getTableName());
        validationErrors.checkRequiredField("columns", statement.getColumnValues());
        validationErrors.checkRequiredField("primaryKey", statement.getPrimaryKey());

        return validationErrors;
    }

    protected String getWhereClause(InsertOrUpdateStatement insertOrUpdateStatement, ExecutionEnvironment env) {

        Database database = env.getTargetDatabase();

        StringBuffer where = new StringBuffer();

        String[] pkColumns = insertOrUpdateStatement.getPrimaryKey().split(",");

        for(String thisPkColumn:pkColumns)
        {
            where.append(database.escapeColumnName(insertOrUpdateStatement.getCatalogName(), insertOrUpdateStatement.getSchemaName(), insertOrUpdateStatement.getTableName(), thisPkColumn)).append(" = ");
            Object newValue = insertOrUpdateStatement.getColumnValues().get(thisPkColumn);
            if (newValue == null || newValue.toString().equals("NULL")) {
                where.append("NULL");
            } else {
                where.append(DataTypeFactory.getInstance().fromObject(newValue, database).objectToSql(newValue, database));
            }

            where.append(" AND ");
        }

        where.delete(where.lastIndexOf(" AND "), where.lastIndexOf(" AND ") + " AND ".length());
        return where.toString();
    }

    protected String getInsertStatement(InsertOrUpdateStatement insertOrUpdateStatement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        StringBuffer insertBuffer = new StringBuffer();
        InsertGenerator insert = new InsertGenerator();
        Action[] insertSql = insert.generateActions(insertOrUpdateStatement, env, chain);

        for(Action action:insertSql) {
            insertBuffer.append(action
                    .describe());
            insertBuffer.append(";");
        }

        insertBuffer.append("\n");

        return insertBuffer.toString();
    }

    protected String getUpdateStatement(InsertOrUpdateStatement insertOrUpdateStatement,ExecutionEnvironment env, String whereClause, StatementLogicChain chain) throws LiquibaseException {

        StringBuffer updateSqlString = new StringBuffer();

        UpdateGenerator update = new UpdateGenerator();
        UpdateStatement updateStatement = new UpdateStatement(
        		insertOrUpdateStatement.getCatalogName(), 
        		insertOrUpdateStatement.getSchemaName(),
        		insertOrUpdateStatement.getTableName());
        updateStatement.setWhereClause(whereClause + ";\n");

        String[] pkFields=insertOrUpdateStatement.getPrimaryKey().split(",");
        HashSet<String> hashPkFields = new HashSet<String>(Arrays.asList(pkFields));
        for(String columnKey:insertOrUpdateStatement.getColumnValues().keySet())
        {
            if (!hashPkFields.contains(columnKey)) {
                updateStatement.addNewColumnValue(columnKey,insertOrUpdateStatement.getColumnValue(columnKey));
            }
        }
        // this isn't very elegant but the code fails above without any columns to update
        if(updateStatement.getNewColumnValues().isEmpty()) {
        	throw new LiquibaseException("No fields to update in set clause");
        }

        Action[] updateSql = update.generateActions(updateStatement, env, chain);

        for(Action s:updateSql)
        {
            updateSqlString.append(s.describe());
            updateSqlString.append(";");
        }

        updateSqlString.deleteCharAt(updateSqlString.lastIndexOf(";"));
        updateSqlString.append("\n");

        return updateSqlString.toString();

    }

    @Override
    public Action[] generateActions(InsertOrUpdateStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        StringBuffer completeSql = new StringBuffer();
        String whereClause = getWhereClause(statement, env);

        completeSql.append( getRecordCheck(statement, env, whereClause));

        completeSql.append(getInsertStatement(statement, env, chain));

        try {

            String updateStatement = getUpdateStatement(statement, env,whereClause,chain);
            
            completeSql.append(getElse(env));

            completeSql.append(updateStatement);
            
        } catch (LiquibaseException e) {}

        completeSql.append(getPostUpdateStatements(env));

        return new Action[]{
                new UnparsedSql(completeSql.toString(), "")
        };
    }
}
