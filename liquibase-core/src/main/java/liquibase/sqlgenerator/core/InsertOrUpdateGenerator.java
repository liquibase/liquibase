package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.exception.ValidationErrors;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.InsertOrUpdateStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;

import java.util.Date;

public abstract class InsertOrUpdateGenerator implements SqlGenerator<InsertOrUpdateStatement> {

    protected abstract String getRecordCheck(InsertOrUpdateStatement insertOrUpdateStatement, Database database, String whereClause);

    protected abstract String getElse(Database database);

    protected String getPostUpdateStatements(){
        return "";
    }

    public int getPriority() {
        return PRIORITY_DATABASE;
    }

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
            where.append(database.escapeColumnName(insertOrUpdateStatement.getSchemaName(), insertOrUpdateStatement.getTableName(), thisPkColumn)).append(" = ");
            Object newValue = insertOrUpdateStatement.getColumnValues().get(thisPkColumn);
            if (newValue == null || newValue.toString().equals("NULL")) {
                where.append("NULL");
            } else if (newValue instanceof String && database.shouldQuoteValue(((String) newValue))) {
                where.append("'").append(database.escapeStringForDatabase((String) newValue)).append("'");
            } else if (newValue instanceof Date) {
                where.append(database.getDateLiteral(((Date) newValue)));
            } else if (newValue instanceof Boolean) {
                if (((Boolean) newValue)) {
                    where.append(TypeConverterFactory.getInstance().findTypeConverter(database).getBooleanType().getTrueBooleanValue());
                } else {
                    where.append(TypeConverterFactory.getInstance().findTypeConverter(database).getBooleanType().getFalseBooleanValue());
                }
            } else {
                where.append(newValue);
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

    protected String getUpdateStatement(InsertOrUpdateStatement insertOrUpdateStatement,Database database, String whereClause, SqlGeneratorChain sqlGeneratorChain){

        StringBuffer updateSqlString = new StringBuffer();

        UpdateGenerator update = new UpdateGenerator();
        UpdateStatement updateStatement = new UpdateStatement(insertOrUpdateStatement.getSchemaName(),insertOrUpdateStatement.getTableName());
        updateStatement.setWhereClause(whereClause + ";\n");


        for(String columnKey:insertOrUpdateStatement.getColumnValues().keySet())
        {
            updateStatement.addNewColumnValue(columnKey,insertOrUpdateStatement.getColumnValue(columnKey));
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

    public Sql[] generateSql(InsertOrUpdateStatement insertOrUpdateStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuffer completeSql = new StringBuffer();
        String whereClause = getWhereClause(insertOrUpdateStatement, database);

        completeSql.append( getRecordCheck(insertOrUpdateStatement, database, whereClause));

        completeSql.append(getInsertStatement(insertOrUpdateStatement, database, sqlGeneratorChain));

        completeSql.append(getElse(database));

        completeSql.append(getUpdateStatement(insertOrUpdateStatement,database,whereClause,sqlGeneratorChain));

        completeSql.append(getPostUpdateStatements());

        return new Sql[]{
                new UnparsedSql(completeSql.toString())
        };
    }
}
