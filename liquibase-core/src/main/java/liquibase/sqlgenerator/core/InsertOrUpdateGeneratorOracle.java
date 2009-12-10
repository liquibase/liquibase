package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.InsertOrUpdateStatement;
import liquibase.statement.core.UpdateStatement;

import java.util.Date;

public class InsertOrUpdateGeneratorOracle extends InsertOrUpdateGenerator {
    public boolean supports(InsertOrUpdateStatement statement, Database database) {
        return database instanceof OracleDatabase;
    }

    public ValidationErrors validate(InsertOrUpdateStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", statement.getTableName());
        validationErrors.checkRequiredField("columns", statement.getColumnValues());
        validationErrors.checkRequiredField("primaryKey", statement.getPrimaryKey());

        return validationErrors;
    }

    public Sql[] generateSql(InsertOrUpdateStatement insertOrUpdateStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuffer completeSql = new StringBuffer();
        String whereClause = getWhereClause(insertOrUpdateStatement, database);

        completeSql.append("DECLARE\n");
        completeSql.append("\tv_reccount NUMBER := 0;\n");
        completeSql.append("BEGIN\n");
        completeSql.append("\tSELECT COUNT(*) INTO v_reccount FROM " + database.escapeTableName(insertOrUpdateStatement.getSchemaName(), insertOrUpdateStatement.getTableName()) + " WHERE ");

        completeSql.append(whereClause);
        completeSql.append(";\n");

        completeSql.append("\tIF v_reccount = 0 THEN\n");

        InsertGenerator insert = new InsertGenerator();
        Sql[] insertSql = insert.generateSql(insertOrUpdateStatement,database,sqlGeneratorChain);

        for(Sql s:insertSql)
        {
            completeSql.append(s.toSql());
            completeSql.append(";");
        }

        completeSql.append("\n");

        completeSql.append("\tELSIF v_reccount = 1 THEN\n");

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
            completeSql.append(s.toSql());
            completeSql.append(";");
        }

        completeSql.deleteCharAt(completeSql.lastIndexOf(";"));
        completeSql.append("\n");

        completeSql.append("END IF;\n");
        completeSql.append("END;\n");

        return new Sql[]{
                new UnparsedSql(completeSql.toString())
        };
    }

    private String getWhereClause(InsertOrUpdateStatement insertOrUpdateStatement, Database database) {
        StringBuffer where = new StringBuffer();

        String[] pkColumns = insertOrUpdateStatement.getPrimaryKey().split(",");

        for(String thisPkColumn:pkColumns)
        {
            where.append(database.escapeColumnName(insertOrUpdateStatement.getSchemaName(), insertOrUpdateStatement.getTableName(), thisPkColumn)  + " = " );
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
}
