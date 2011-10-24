package liquibase.change.core;

import liquibase.change.ChangeClass;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeProperty;
import liquibase.database.Database;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.exception.LiquibaseException;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DeleteStatement;
import liquibase.statement.core.InsertOrUpdateStatement;
import liquibase.statement.core.InsertStatement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ChangeClass(name="loadUpdateData", description = "Smart Load Data", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
public class LoadUpdateDataChange extends LoadDataChange {
    private String primaryKey;

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return super.generateStatements(database);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public void setPrimaryKey(String primaryKey) throws LiquibaseException {
        if (primaryKey == null) {
            throw new LiquibaseException("primaryKey cannot be null.");
        }
        this.primaryKey = primaryKey;
    }

    @ChangeProperty(requiredForDatabase = "all")
    public String getPrimaryKey() {
        return primaryKey;
    }

    @Override
    protected InsertStatement createStatement(String schemaName, String tableName) {
        return new InsertOrUpdateStatement(schemaName, tableName, this.primaryKey);
    }

    @Override
    public SqlStatement[] generateRollbackStatements(Database database) throws UnsupportedChangeException, RollbackImpossibleException {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        SqlStatement[] forward = this.generateStatements(database);

        for(SqlStatement thisForward: forward){
            InsertOrUpdateStatement thisInsert = (InsertOrUpdateStatement)thisForward;
            DeleteStatement delete = new DeleteStatement(getSchemaName(),getTableName());
            delete.setWhereClause(getWhereClause(thisInsert,database));
            statements.add(delete);
        }

        return statements.toArray(new SqlStatement[statements.size()]);
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
