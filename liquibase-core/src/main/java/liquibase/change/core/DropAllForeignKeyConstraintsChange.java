package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import  liquibase.ExecutionEnvironment;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.Row;
import liquibase.statement.Statement;
import liquibase.statement.core.FindForeignKeyConstraintsStatement;
import liquibase.structure.core.Table;

import java.util.*;

@DatabaseChange(name="dropAllForeignKeyConstraints", description = "Drops all foreign key constraints for a table", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
public class DropAllForeignKeyConstraintsChange extends AbstractChange {

    private String baseTableCatalogName;
    private String baseTableSchemaName;
    private String baseTableName;

    @DatabaseChangeProperty(mustEqualExisting ="table.catalog", description = "Name of the table containing columns constrained by foreign keys", since = "3.0")
    public String getBaseTableCatalogName() {
        return baseTableCatalogName;
    }

    public void setBaseTableCatalogName(String baseTableCatalogName) {
        this.baseTableCatalogName = baseTableCatalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="table.schema")
    public String getBaseTableSchemaName() {
        return baseTableSchemaName;
    }

    public void setBaseTableSchemaName(String baseTableSchemaName) {
        this.baseTableSchemaName = baseTableSchemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "table", requiredForDatabase = "all")
    public String getBaseTableName() {
        return baseTableName;
    }

    public void setBaseTableName(String baseTableName) {
        this.baseTableName = baseTableName;
    }

    @Override
    public Statement[] generateStatements(ExecutionEnvironment env) {
        List<Statement> statements = new ArrayList<Statement>();

        List<DropForeignKeyConstraintChange> childDropChanges = generateChildren(env);

        if (childDropChanges != null) {
            for (DropForeignKeyConstraintChange change : childDropChanges) {
                statements.addAll(Arrays.asList(change.generateStatements(env)));
            }
        }

        return statements.toArray(new Statement[statements.size()]);
    }

    @Override
    public String getConfirmationMessage() {
        return "Foreign keys on base table " + getBaseTableName() + " dropped";
    }

    private List<DropForeignKeyConstraintChange> generateChildren(ExecutionEnvironment env) {
        // Make a new list
        List<DropForeignKeyConstraintChange> childDropChanges = new ArrayList<DropForeignKeyConstraintChange>();

        Database database = env.getTargetDatabase();
        Executor executor = ExecutorService.getInstance().getExecutor(database);

        FindForeignKeyConstraintsStatement sql = new FindForeignKeyConstraintsStatement(getBaseTableCatalogName(), getBaseTableSchemaName(), getBaseTableName());

        try {
            List<Row> results = executor.query(sql).toList();
            Set<String> handledConstraints = new HashSet<String>();

            if (results != null && results.size() > 0) {
                for (Row result : results) {
                    String baseTableName = result.get(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_NAME, String.class);
                    String constraintName = result.get(FindForeignKeyConstraintsStatement.RESULT_COLUMN_CONSTRAINT_NAME, String.class);
                    if (DatabaseObjectComparatorFactory.getInstance().isSameObject(new Table().setName(getBaseTableName()), new Table().setName(baseTableName), database)) {
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

            return childDropChanges;

        } catch (DatabaseException e) {
            throw new UnexpectedLiquibaseException("Failed to find foreign keys for table: " + getBaseTableName(), e);
        }
    }

    @Override
    public boolean generateStatementsVolatile(ExecutionEnvironment env) {
        return true;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
