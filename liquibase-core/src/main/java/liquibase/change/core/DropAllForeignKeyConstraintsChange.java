package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.DatabaseChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.structure.core.Table;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.FindForeignKeyConstraintsStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@DatabaseChange(name="dropAllForeignKeyConstraints", description = "Drop All Foreign Key Constraints", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
public class DropAllForeignKeyConstraintsChange extends AbstractChange {

    private String baseTableCatalogName;
    private String baseTableSchemaName;
    private String baseTableName;

    @DatabaseChangeProperty(includeInSerialization = false)
    private List<DropForeignKeyConstraintChange> childDropChanges;

    @DatabaseChangeProperty(mustApplyTo ="table.catalog")
    public String getBaseTableCatalogName() {
        return baseTableCatalogName;
    }

    public void setBaseTableCatalogName(String baseTableCatalogName) {
        this.baseTableCatalogName = baseTableCatalogName;
    }

    @DatabaseChangeProperty(mustApplyTo ="table.schema")
    public String getBaseTableSchemaName() {
        return baseTableSchemaName;
    }

    public void setBaseTableSchemaName(String baseTableSchemaName) {
        this.baseTableSchemaName = baseTableSchemaName;
    }

    @DatabaseChangeProperty(requiredForDatabase = "all", mustApplyTo = "table")
    public String getBaseTableName() {
        return baseTableName;
    }

    public void setBaseTableName(String baseTableName) {
        this.baseTableName = baseTableName;
    }

    public SqlStatement[] generateStatements(Database database) {
        List<SqlStatement> sqlStatements = new ArrayList<SqlStatement>();

        if (childDropChanges == null) {
            generateChildren(database);
        }

        if (childDropChanges != null) {
            for (DropForeignKeyConstraintChange change : childDropChanges) {
                sqlStatements.addAll(Arrays.asList(change.generateStatements(database)));
            }
        }

        return sqlStatements.toArray(new SqlStatement[sqlStatements.size()]);
    }

    public String getConfirmationMessage() {
        return "Foreign keys on base table " + getBaseTableName() + " dropped";
    }

    private void generateChildren(Database database) {
        // Make a new list
        childDropChanges = new ArrayList<DropForeignKeyConstraintChange>();

        Executor executor = ExecutorService.getInstance().getExecutor(database);

        FindForeignKeyConstraintsStatement sql = new FindForeignKeyConstraintsStatement(getBaseTableCatalogName(), getBaseTableSchemaName(), getBaseTableName());

        try {
            List<Map> results = executor.queryForList(sql);
            Set<String> handledConstraints = new HashSet<String>();

            if (results != null && results.size() > 0) {
                for (Map result : results) {
                    String baseTableName =
                            (String) result.get(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_NAME);
                    String constraintName =
                            (String) result.get(FindForeignKeyConstraintsStatement.RESULT_COLUMN_CONSTRAINT_NAME);
                    if (new Table(getBaseTableName()).equals(new Table(baseTableName), database)) {
                        if( !handledConstraints.contains(constraintName)) {
                            DropForeignKeyConstraintChange dropForeignKeyConstraintChange =
                                    new DropForeignKeyConstraintChange();

                            dropForeignKeyConstraintChange.setBaseTableSchemaName(getBaseTableSchemaName());
                            dropForeignKeyConstraintChange.setBaseTableName(baseTableName);
                            dropForeignKeyConstraintChange.setConstraintName(constraintName);

                            childDropChanges.add(dropForeignKeyConstraintChange);
                            handledConstraints.add(constraintName);
                        }
                    } else {
                        throw new IllegalStateException("Expected to return only foreign keys for base table name: " +
                                getBaseTableName() + " and got results for table: " + baseTableName);
                    }
                }
            }
        } catch (DatabaseException e) {
            throw new UnexpectedLiquibaseException("Failed to find foreign keys for table: " + getBaseTableName(), e);
        }
    }

    @Override
    public boolean queriesDatabase(Database database) {
        return true;
    }
}
