package liquibase.sqlgenerator.core;

import liquibase.ExecutionEnvironment;
import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statement.core.UpdateDataStatement;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.LiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.statement.core.InsertOrUpdateDataStatement;

import java.util.Arrays;
import java.util.HashSet;

public abstract class InsertOrUpdateGenerator extends AbstractSqlGenerator<InsertOrUpdateDataStatement> {

    protected abstract String getRecordCheck(InsertOrUpdateDataStatement insertOrUpdateDataStatement, ExecutionEnvironment env, String whereClause);

    protected abstract String getElse(ExecutionEnvironment env);

    protected String getPostUpdateStatements(ExecutionEnvironment env){
        return "";
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public ValidationErrors validate(InsertOrUpdateDataStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", statement.getTableName());
        validationErrors.checkRequiredField("columns", statement.getColumnNames());
        validationErrors.checkRequiredField("primaryKey", statement.getPrimaryKey());

        return validationErrors;
    }

    protected String getWhereClause(InsertOrUpdateDataStatement insertOrUpdateDataStatement, ExecutionEnvironment env) {

        Database database = env.getTargetDatabase();

        StringBuffer where = new StringBuffer();

        String[] pkColumns = insertOrUpdateDataStatement.getPrimaryKey().split(",");

        for(String thisPkColumn:pkColumns)
        {
            where.append(database.escapeColumnName(insertOrUpdateDataStatement.getCatalogName(), insertOrUpdateDataStatement.getSchemaName(), insertOrUpdateDataStatement.getTableName(), thisPkColumn)).append(" = ");
            Object newValue = insertOrUpdateDataStatement.getColumnValue(thisPkColumn);
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

    protected String getInsertStatement(InsertOrUpdateDataStatement insertOrUpdateDataStatement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        StringBuffer insertBuffer = new StringBuffer();
        InsertGenerator insert = new InsertGenerator();
        Action[] insertSql = insert.generateActions(insertOrUpdateDataStatement, env, chain);

        for(Action action:insertSql) {
            insertBuffer.append(action
                    .describe());
            insertBuffer.append(";");
        }

        insertBuffer.append("\n");

        return insertBuffer.toString();
    }

    protected String getUpdateStatement(InsertOrUpdateDataStatement insertOrUpdateDataStatement,ExecutionEnvironment env, String whereClause, StatementLogicChain chain) throws LiquibaseException {

        StringBuffer updateSqlString = new StringBuffer();

        UpdateGenerator update = new UpdateGenerator();
        UpdateDataStatement updateDataStatement = new UpdateDataStatement(
        		insertOrUpdateDataStatement.getCatalogName(),
        		insertOrUpdateDataStatement.getSchemaName(),
        		insertOrUpdateDataStatement.getTableName());
        updateDataStatement.setWhere(whereClause + ";\n");

        String[] pkFields= insertOrUpdateDataStatement.getPrimaryKey().split(",");
        HashSet<String> hashPkFields = new HashSet<String>(Arrays.asList(pkFields));
        for(String columnKey: insertOrUpdateDataStatement.getColumnNames())
        {
            if (!hashPkFields.contains(columnKey)) {
                updateDataStatement.addNewColumnValue(columnKey, insertOrUpdateDataStatement.getColumnValue(columnKey));
            }
        }
        // this isn't very elegant but the code fails above without any columns to update
        if(updateDataStatement.getColumnNames().isEmpty()) {
        	throw new LiquibaseException("No fields to update in set clause");
        }

        Action[] updateSql = update.generateActions(updateDataStatement, env, chain);

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
    public Action[] generateActions(InsertOrUpdateDataStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
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
