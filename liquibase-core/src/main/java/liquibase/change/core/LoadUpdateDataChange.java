package liquibase.change.core;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.ChangeClass;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeProperty;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DeleteStatement;
import liquibase.statement.core.InsertOrUpdateStatement;
import liquibase.statement.core.InsertStatement;

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
    protected InsertStatement createStatement(String catalogName, String schemaName, String tableName) {
        return new InsertOrUpdateStatement(catalogName, schemaName, tableName, this.primaryKey);
    }

    @Override
    public SqlStatement[] generateRollbackStatements(Database database) throws UnsupportedChangeException, RollbackImpossibleException {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        SqlStatement[] forward = this.generateStatements(database);

        for(SqlStatement thisForward: forward){
            InsertOrUpdateStatement thisInsert = (InsertOrUpdateStatement)thisForward;
            DeleteStatement delete = new DeleteStatement(getCatalogName(), getSchemaName(),getTableName());
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
            where.append(database.escapeColumnName(insertOrUpdateStatement.getCatalogName(), insertOrUpdateStatement.getSchemaName(), insertOrUpdateStatement.getTableName(), thisPkColumn)  + " = " );
            Object newValue = insertOrUpdateStatement.getColumnValues().get(thisPkColumn);
            where.append(database.getDataTypeFactory().fromObject(newValue, database));

            where.append(" AND ");
        }

        where.delete(where.lastIndexOf(" AND "),where.lastIndexOf(" AND ") + " AND ".length());
        return where.toString();
    }

}
