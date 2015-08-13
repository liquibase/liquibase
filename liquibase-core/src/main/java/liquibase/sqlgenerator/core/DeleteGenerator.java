package liquibase.sqlgenerator.core;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DeleteStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;

public class DeleteGenerator extends AbstractSqlGenerator<DeleteStatement> {

    @Override
    public ValidationErrors validate(DeleteStatement deleteStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", deleteStatement.getTableName());
        if (deleteStatement.getWhereParameters() != null && deleteStatement.getWhereParameters().size() > 0 && deleteStatement.getWhere() == null) {
            validationErrors.addError("whereParams set but no whereClause");
        }
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DeleteStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuffer sql = new StringBuffer("DELETE FROM " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()));

        if (statement.getWhere() != null) {
            String fixedWhereClause = "WHERE " + statement.getWhere().trim();
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

        return new Sql[] { new UnparsedSql(sql.toString(), getAffectedTable(statement)) };
    }

    protected Relation getAffectedTable(DeleteStatement statement) {
        return new Table().setName(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }
}
