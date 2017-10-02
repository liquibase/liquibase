package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.LiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.InsertOrUpdateStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.structure.core.Table;

import java.util.Arrays;
import java.util.HashSet;

public abstract class InsertOrUpdateGenerator extends AbstractSqlGenerator<InsertOrUpdateStatement> {

    protected abstract String getRecordCheck(InsertOrUpdateStatement insertOrUpdateStatement, Database database, String whereClause);

    protected abstract String getElse(Database database);

    protected String getPostUpdateStatements(Database database){
        return "";
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public ValidationErrors validate(InsertOrUpdateStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", statement.getTableName());
        validationErrors.checkRequiredField("columns", statement.getColumnValues());
        validationErrors.checkRequiredField("primaryKey", statement.getPrimaryKey());

        return validationErrors;
    }

    protected String getWhereClause(InsertOrUpdateStatement insertOrUpdateStatement, Database database) {
        StringBuffer where = new StringBuffer();

        String[] pkColumns = insertOrUpdateStatement.getPrimaryKey().split(",");

        for(String thisPkColumn:pkColumns)
        {
            Object newValue = insertOrUpdateStatement.getColumnValues().get(thisPkColumn);
            where.append(database.escapeColumnName(insertOrUpdateStatement.getCatalogName(),
                        insertOrUpdateStatement.getSchemaName(),
                        insertOrUpdateStatement.getTableName(),
                        thisPkColumn)).append(((newValue == null) || "NULL".equalsIgnoreCase(newValue.toString())) ?
                " is " : " = ");

            if ((newValue == null) || "NULL".equalsIgnoreCase(newValue.toString())) {
                where.append("NULL");
            } else {
                where.append(DataTypeFactory.getInstance().fromObject(newValue, database).objectToSql(newValue, database));
            }

            where.append(" AND ");
        }

        where.delete(where.lastIndexOf(" AND "),where.lastIndexOf(" AND ") + " AND ".length());
        return where.toString();
    }

    protected String getInsertStatement(InsertOrUpdateStatement insertOrUpdateStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuffer insertBuffer = new StringBuffer();
        InsertGenerator insert = new InsertGenerator();
        Sql[] insertSql = insert.generateSql(insertOrUpdateStatement,database,sqlGeneratorChain);

        for(Sql s:insertSql)
        {
            insertBuffer.append(s.toSql());
            insertBuffer.append(";");
        }

        insertBuffer.append("\n");

        return insertBuffer.toString();
    }

    /**
     * 
     * @param insertOrUpdateStatement
     * @param database
     * @param whereClause
     * @param sqlGeneratorChain
     * @return the update statement, if there is nothing to update return null
     */
    protected String getUpdateStatement(InsertOrUpdateStatement insertOrUpdateStatement,Database database, String whereClause, SqlGeneratorChain sqlGeneratorChain) throws LiquibaseException {

        StringBuffer updateSqlString = new StringBuffer();

        UpdateGenerator update = new UpdateGenerator();
        UpdateStatement updateStatement = new UpdateStatement(
        		insertOrUpdateStatement.getCatalogName(), 
        		insertOrUpdateStatement.getSchemaName(),
        		insertOrUpdateStatement.getTableName());
        if (!((database instanceof OracleDatabase) && (insertOrUpdateStatement.getOnlyUpdate() != null) &&
            insertOrUpdateStatement.getOnlyUpdate())) {
            whereClause += ";\n";
        }

        updateStatement.setWhereClause(whereClause);

        String[] pkFields=insertOrUpdateStatement.getPrimaryKey().split(",");
        HashSet<String> hashPkFields = new HashSet<>(Arrays.asList(pkFields));
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

        Sql[] updateSql = update.generateSql(updateStatement, database, sqlGeneratorChain);

        for(Sql s:updateSql)
        {
            updateSqlString.append(s.toSql());
            updateSqlString.append(";");
        }

        updateSqlString.deleteCharAt(updateSqlString.lastIndexOf(";"));
        updateSqlString.append("\n");

        return updateSqlString.toString();

    }

    @Override
    public Sql[] generateSql(InsertOrUpdateStatement insertOrUpdateStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuffer completeSql = new StringBuffer();
        String whereClause = getWhereClause(insertOrUpdateStatement, database);
        if ( !insertOrUpdateStatement.getOnlyUpdate() ) {
	        completeSql.append( getRecordCheck(insertOrUpdateStatement, database, whereClause));
	
	        completeSql.append(getInsertStatement(insertOrUpdateStatement, database, sqlGeneratorChain));
        }
        try {
        	
            String updateStatement = getUpdateStatement(insertOrUpdateStatement,database,whereClause,sqlGeneratorChain);
            
            if ( !insertOrUpdateStatement.getOnlyUpdate() ) {
            	completeSql.append(getElse(database));
            }

            completeSql.append(updateStatement);
            
        } catch (LiquibaseException e) {}

        if ( !insertOrUpdateStatement.getOnlyUpdate() ) {
        	completeSql.append(getPostUpdateStatements(database));
        }

        return new Sql[]{
                new UnparsedSql(completeSql.toString(), "", getAffectedTable(insertOrUpdateStatement))
        };
    }

    protected Table getAffectedTable(InsertOrUpdateStatement insertOrUpdateStatement) {
        return (Table) new Table().setName(insertOrUpdateStatement.getTableName()).setSchema(insertOrUpdateStatement.getCatalogName(), insertOrUpdateStatement.getSchemaName());
    }
}
