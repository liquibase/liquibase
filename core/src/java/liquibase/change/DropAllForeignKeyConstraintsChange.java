package liquibase.change;

import liquibase.database.Database;
import liquibase.database.template.Executor;
import liquibase.exception.JDBCException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.FindForeignKeyConstraintsStatement;
import liquibase.statement.SqlStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DropAllForeignKeyConstraintsChange extends AbstractChange {

    private String baseTableSchemaName;
    private String baseTableName;

    private List<DropForeignKeyConstraintChange> childDropChanges;


    public DropAllForeignKeyConstraintsChange() {
        super("dropAllForeignKeyConstraints", "Drop All Foreign Key Constraints", ChangeMetaData.PRIORITY_DEFAULT);
    }

    public String getBaseTableSchemaName() {
        return baseTableSchemaName;
    }

    public void setBaseTableSchemaName(String baseTableSchemaName) {
        this.baseTableSchemaName = baseTableSchemaName;
    }

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

        Executor jdbc = database.getExecutor();

        FindForeignKeyConstraintsStatement sql = new FindForeignKeyConstraintsStatement(
                getBaseTableSchemaName(),
                getBaseTableName()
        );

        try {
            List<Map> results = jdbc.queryForList(sql, new ArrayList<SqlVisitor>());

            if (results != null && results.size() > 0) {
                for (Map result : results) {
                    String baseTableName =
                            (String) result.get(FindForeignKeyConstraintsStatement.RESULT_COLUMN_BASE_TABLE_NAME);
                    String constraintName =
                            (String) result.get(FindForeignKeyConstraintsStatement.RESULT_COLUMN_CONSTRAINT_NAME);
                    if (getBaseTableName().equals(baseTableName)) {
                        DropForeignKeyConstraintChange dropForeignKeyConstraintChange =
                                new DropForeignKeyConstraintChange();

                        dropForeignKeyConstraintChange.setBaseTableSchemaName(getBaseTableSchemaName());
                        dropForeignKeyConstraintChange.setBaseTableName(baseTableName);
                        dropForeignKeyConstraintChange.setConstraintName(constraintName);

                        childDropChanges.add(dropForeignKeyConstraintChange);
                    } else {
                        throw new IllegalStateException("Expected to return only foreign keys for base table name: " +
                                getBaseTableName() + " and got results for table: " + baseTableName);
                    }
                }
            }
        } catch (JDBCException e) {
            throw new UnexpectedLiquibaseException("Failed to find foreign keys for table: " + getBaseTableName(), e);
        }
    }
}
