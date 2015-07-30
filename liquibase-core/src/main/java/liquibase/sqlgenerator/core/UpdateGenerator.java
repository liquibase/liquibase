package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.core.UpdateStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;

import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateGenerator extends AbstractSqlGenerator<UpdateStatement> {

    @Override
    public ValidationErrors validate(UpdateStatement updateStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", updateStatement.getTableName());
        validationErrors.checkRequiredField("columns", updateStatement.getNewColumnValues());
        if (updateStatement.getWhereParameters() != null && updateStatement.getWhereParameters().size() > 0 && updateStatement.getWhereClause() == null) {
            validationErrors.addError("whereParams set but no whereClause");
        }
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(UpdateStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuffer sql = new StringBuffer("UPDATE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " SET");
        for (String column : statement.getNewColumnValues().keySet()) {
            sql.append(" ").append(database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), column)).append(" = ");
            sql.append(convertToString(statement.getNewColumnValues().get(column), database));
            sql.append(",");
        }

        int lastComma = sql.lastIndexOf(",");
        if (lastComma >= 0) {
            sql.deleteCharAt(lastComma);
        }
        if (statement.getWhereClause() != null) {
            String fixedWhereClause = "WHERE " + statement.getWhereClause().trim();
            Matcher matcher = Pattern.compile(":name|\\?|:value").matcher(fixedWhereClause);
            StringBuffer sb = new StringBuffer();
            Iterator<String> columnNameIter = statement.getWhereColumnNames().iterator();
            Iterator<Object> paramIter = statement.getWhereParameters().iterator();
            while (matcher.find()) {
                if (matcher.group().equals(":name")) {
                    while (columnNameIter.hasNext()) {
                        String columnName = columnNameIter.next();
                        if (columnName == null) {
                            continue;
                        }
                        matcher.appendReplacement(sb, Matcher.quoteReplacement(database.escapeObjectName(columnName, Column.class)));
                        break;
                    }
                } else if (paramIter.hasNext()) {
                    Object param = paramIter.next();
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(DataTypeFactory.getInstance().fromObject(param, database).objectToSql(param, database)));
                }
            }
            matcher.appendTail(sb);
            fixedWhereClause = sb.toString();
            sql.append(" ").append(fixedWhereClause);
        }

        return new Sql[] {
                new UnparsedSql(sql.toString(), getAffectedTable(statement))
        };
    }

    protected Relation getAffectedTable(UpdateStatement statement) {
        return new Table().setName(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }

    private String convertToString(Object newValue, Database database) {
        String sqlString;
        if (newValue == null || newValue.toString().equalsIgnoreCase("NULL")) {
            sqlString = "NULL";
        } else if (newValue instanceof String && !looksLikeFunctionCall(((String) newValue), database)) {
            sqlString = DataTypeFactory.getInstance().fromObject(newValue, database).objectToSql(newValue, database);
        } else if (newValue instanceof Date) {
            // converting java.util.Date to java.sql.Date
            Date date = (Date) newValue;
            if (date.getClass().equals(java.util.Date.class)) {
                date = new java.sql.Date(date.getTime());
            }

            sqlString = database.getDateLiteral(date);
        } else if (newValue instanceof Boolean) {
            if (((Boolean) newValue)) {
                sqlString = DataTypeFactory.getInstance().getTrueBooleanValue(database);
            } else {
                sqlString = DataTypeFactory.getInstance().getFalseBooleanValue(database);
            }
        } else if (newValue instanceof DatabaseFunction) {
            sqlString = database.generateDatabaseFunctionValue((DatabaseFunction) newValue);
        } else {
            sqlString = newValue.toString();
        }
        return sqlString;
    }
}
