package liquibase.change.core;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.LiquibaseException;
import liquibase.exception.RollbackImpossibleException;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.Statement;
import liquibase.statement.core.DeleteDataStatement;
import liquibase.statement.core.InsertDataStatement;
import liquibase.statement.core.InsertOrUpdateDataStatement;

import java.util.ArrayList;
import java.util.List;

@DatabaseChange(name="loadUpdateData",
        description = "Loads or updates data from a CSV file into an existing table. Differs from loadData by issuing a SQL batch that checks for the existence of a record. If found, the record is UPDATEd, else the record is INSERTed. Also, generates DELETE statements for a rollback.\n" +
                "\n" +
                "A value of NULL in a cell will be converted to a database NULL rather than the string 'NULL'",
        priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table", since = "2.0")
public class LoadUpdateDataChange extends LoadDataChange {
    private String primaryKey;

    @Override
    @DatabaseChangeProperty(description = "Name of the table to insert or update data in", requiredForDatabase = "all")
    public String getTableName() {
        return super.getTableName();
    }

    public void setPrimaryKey(String primaryKey) throws LiquibaseException {
        this.primaryKey = primaryKey;
    }

    @DatabaseChangeProperty(description = "Comma delimited list of the columns for the primary key", requiredForDatabase = "all")
    public String getPrimaryKey() {
        return primaryKey;
    }

    @Override
    protected InsertDataStatement createStatement(String catalogName, String schemaName, String tableName) {
        return new InsertOrUpdateDataStatement(catalogName, schemaName, tableName, this.primaryKey);
    }

    @Override
    public Statement[] generateRollbackStatements(ExecutionEnvironment env) throws RollbackImpossibleException {
        List<Statement> statements = new ArrayList<Statement>();
        Statement[] forward = this.generateStatements(env);

        for(Statement thisForward: forward){
            InsertOrUpdateDataStatement thisInsert = (InsertOrUpdateDataStatement)thisForward;
            DeleteDataStatement delete = new DeleteDataStatement(getCatalogName(), getSchemaName(),getTableName());
            delete.setWhere(getWhere(thisInsert, env.getTargetDatabase()));
            statements.add(delete);
        }

        return statements.toArray(new Statement[statements.size()]);
    }

    private String getWhere(InsertOrUpdateDataStatement insertOrUpdateDataStatement, Database database) {
        StringBuilder where = new StringBuilder();

        String[] pkColumns = insertOrUpdateDataStatement.getPrimaryKey().split(",");

        for(String thisPkColumn:pkColumns)
        {
            where.append(database.escapeColumnName(insertOrUpdateDataStatement.getCatalogName(), insertOrUpdateDataStatement.getSchemaName(), insertOrUpdateDataStatement.getTableName(), thisPkColumn)).append(" = ");
            Object newValue = insertOrUpdateDataStatement.getColumnValue(thisPkColumn);
            where.append(DataTypeFactory.getInstance().fromObject(newValue, database).objectToSql(newValue, database));

            where.append(" AND ");
        }

        where.delete(where.lastIndexOf(" AND "),where.lastIndexOf(" AND ") + " AND ".length());
        return where.toString();
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public ChangeStatus checkStatus(ExecutionEnvironment env) {
        return new ChangeStatus().unknown("Cannot check loadUpdateData status");
    }
}
