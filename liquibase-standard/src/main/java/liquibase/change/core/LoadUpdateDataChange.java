package liquibase.change.core;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.RollbackImpossibleException;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.BatchDmlExecutablePreparedStatement;
import liquibase.statement.ExecutablePreparedStatementBase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DeleteStatement;
import liquibase.statement.core.InsertOrUpdateStatement;
import liquibase.statement.core.InsertStatement;
import liquibase.util.StringUtil;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static liquibase.change.ChangeParameterMetaData.ALL;

@DatabaseChange(name = "loadUpdateData",
        description = "Loads or updates data from a CSV file into an existing table. Differs from loadData by " +
                "issuing a SQL batch that checks for the existence of a record. If found, the record is UPDATEd, " +
                "else the record is INSERTed. Also, generates DELETE statements for a rollback.\n" +
                "\n" +
                "A value of NULL in a cell will be converted to a database NULL rather than the string 'NULL'",
        priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table", since = "2.0")
public class LoadUpdateDataChange extends LoadDataChange {
    @Setter
    private String primaryKey;
    private Boolean onlyUpdate = Boolean.FALSE;

    @Override
    protected boolean hasPreparedStatementsImplemented() { return false; }

    @Override
    @DatabaseChangeProperty(description = "Name of the table to insert or update data in", requiredForDatabase = ALL)
    public String getTableName() {
        return super.getTableName();
    }

    @DatabaseChangeProperty(description = "Comma-delimited list of columns for the primary key",
            requiredForDatabase = ALL)
    public String getPrimaryKey() {
        return primaryKey;
    }

    @DatabaseChangeProperty(description = "If true, records with no matching database record should be ignored",
            since = "3.3", supportsDatabase = ALL)
    public Boolean getOnlyUpdate() {
        if (onlyUpdate == null) {
            return false;
        }
        return onlyUpdate;
    }

    public void setOnlyUpdate(Boolean onlyUpdate) {
        this.onlyUpdate = ((onlyUpdate == null) ? Boolean.FALSE : onlyUpdate);
    }

    /**
     * Creates a {@link liquibase.statement.core.InsertOrUpdateStatement} statement object for the specified table
     * @param catalogName name of the catalog where the table exists
     * @param schemaName name of the schema where the table exists
     * @param tableName the table name to insert/update data
     * @return a specialised {@link liquibase.statement.core.InsertOrUpdateStatement} that will either insert or update rows in the target table
     */
    @Override
    protected InsertStatement createStatement(String catalogName, String schemaName, String tableName) {
        return new InsertOrUpdateStatement(catalogName, schemaName, tableName, this.primaryKey, this.getOnlyUpdate());
    }

    @Override
    protected ExecutablePreparedStatementBase createPreparedStatement(
            Database database, String catalogName, String schemaName, String tableName,
            List<LoadDataColumnConfig> columns, ChangeSet changeSet, ResourceAccessor resourceAccessor) {
        // TODO: Not supported yet. When this is implemented, we can remove hasPreparedStatementsImplemented().
        throw new UnsupportedOperationException("Executable Prepared Statements are not supported for " +
                "LoadUpdateDataChange yet. Very sorry.");
    }

    @Override
    public SqlStatement[] generateRollbackStatements(Database database) throws RollbackImpossibleException {
        List<SqlStatement> statements = new ArrayList<>();
        List<SqlStatement> finalForwardList = new ArrayList<>();

        // If we are dealing with a batched UPDATE, "unroll" the individual statements first.
        for (SqlStatement thisForward : this.generateStatements(database)) {
            if (thisForward instanceof BatchDmlExecutablePreparedStatement) {
                finalForwardList.addAll(
                        ((BatchDmlExecutablePreparedStatement)thisForward).getIndividualStatements()
                );
            } else {
                finalForwardList.add(thisForward);
            }
        }

        for (SqlStatement thisForward : finalForwardList) {
            InsertOrUpdateStatement thisInsert = (InsertOrUpdateStatement) thisForward;
            DeleteStatement delete = new DeleteStatement(getCatalogName(), getSchemaName(), getTableName());
            delete.setWhere(getWhere(thisInsert, database));
            statements.add(delete);
        }

        return statements.toArray(SqlStatement.EMPTY_SQL_STATEMENT);
    }

    private String getWhere(InsertOrUpdateStatement insertOrUpdateStatement, Database database) {
        StringBuilder where = new StringBuilder();

        String[] pkColumns = insertOrUpdateStatement.getPrimaryKey().split(",");

        for (String thisPkColumn : pkColumns) {
            Object newValue = insertOrUpdateStatement.getColumnValues().get(thisPkColumn);
            where.append(database.escapeColumnName(insertOrUpdateStatement.getCatalogName(),
                    insertOrUpdateStatement.getSchemaName(),
                    insertOrUpdateStatement.getTableName(),
                    thisPkColumn)).append(((newValue == null) || StringUtil.equalsWordNull(newValue.toString())) ? " is " : " = ");

            if ((newValue == null) || StringUtil.equalsWordNull(newValue.toString())) {
                where.append("NULL");
            } else {
                where.append(DataTypeFactory.getInstance().fromObject(newValue, database).objectToSql(newValue,
                        database));
            }

            where.append(" AND ");
        }

        where.delete(where.lastIndexOf(" AND "), where.lastIndexOf(" AND ") + " AND ".length());
        return where.toString();
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        return new ChangeStatus().unknown("Cannot check loadUpdateData status");
    }
}
