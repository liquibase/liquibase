package liquibase.change.core;

import liquibase.change.DatabaseChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.LiquibaseException;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DeleteStatement;
import liquibase.statement.core.InsertOrUpdateStatement;
import liquibase.statement.core.InsertStatement;

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
    protected InsertStatement createStatement(String catalogName, String schemaName, String tableName) {
        return new InsertOrUpdateStatement(catalogName, schemaName, tableName, this.primaryKey);
    }

    @Override
    public SqlStatement[] generateRollbackStatements(Database database) throws RollbackImpossibleException {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        SqlStatement[] forward = this.generateStatements(database);

        for(SqlStatement thisForward: forward){
            InsertOrUpdateStatement thisInsert = (InsertOrUpdateStatement)thisForward;
            DeleteStatement delete = new DeleteStatement(getCatalogName(), getSchemaName(),getTableName());
            delete.setWhere(getWhere(thisInsert,database));
            statements.add(delete);
        }

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    private String getWhere(InsertOrUpdateStatement insertOrUpdateStatement, Database database) {
        StringBuilder where = new StringBuilder();

        String[] pkColumns = insertOrUpdateStatement.getPrimaryKey().split(",");

        for(String thisPkColumn:pkColumns)
        {
            where.append(database.escapeColumnName(insertOrUpdateStatement.getCatalogName(), insertOrUpdateStatement.getSchemaName(), insertOrUpdateStatement.getTableName(), thisPkColumn)).append(" = ");
            Object newValue = insertOrUpdateStatement.getColumnValues().get(thisPkColumn);
            where.append(DataTypeFactory.getInstance().fromObject(newValue, database).objectToSql(newValue, database));

            where.append(" AND ");
        }

        where.delete(where.lastIndexOf(" AND "),where.lastIndexOf(" AND ") + " AND ".length());
        return where.toString();
    }

}
