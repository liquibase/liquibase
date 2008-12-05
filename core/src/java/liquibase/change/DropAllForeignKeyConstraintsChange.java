package liquibase.change;

import liquibase.database.Database;
import liquibase.database.sql.SqlStatement;
import liquibase.database.sql.FindForeignKeyConstraintsStatement;
import liquibase.database.sql.visitor.SqlStatementVisitor;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.template.JdbcTemplate;
import liquibase.exception.JDBCException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

public class DropAllForeignKeyConstraintsChange extends AbstractChange {

    private String baseTableSchemaName;
    private String baseTableName;

    private List<DropForeignKeyConstraintChange> childDropChanges;


    public DropAllForeignKeyConstraintsChange() {
        super("dropAllForeignKeyConstraints", "Drop All Foreign Key Constraints");
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

    public void validate(Database database) throws InvalidChangeDefinitionException {
        if (StringUtils.trimToNull(baseTableName) == null) {
            throw new InvalidChangeDefinitionException("baseTableName is required", this);
        }
    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
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

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement(getTagName());

        if (getBaseTableSchemaName() != null) {
            node.setAttribute("baseTableSchemaName", getBaseTableSchemaName());
        }

        node.setAttribute("baseTableName", getBaseTableName());

        return node;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        Set<DatabaseObject> databaseObjects = null;

        if (childDropChanges != null) {
            databaseObjects = new HashSet<DatabaseObject>();
            for (DropForeignKeyConstraintChange change : childDropChanges) {
                databaseObjects.addAll(change.getAffectedDatabaseObjects());
            }
        }

        return databaseObjects;
    }

    private void generateChildren(Database database) throws UnsupportedChangeException {
        // Make a new list
        childDropChanges = new ArrayList<DropForeignKeyConstraintChange>();

        JdbcTemplate jdbc = database.getJdbcTemplate();

        FindForeignKeyConstraintsStatement sql = new FindForeignKeyConstraintsStatement(
                getBaseTableSchemaName(),
                getBaseTableName()
        );

        try {
            List<Map> results = jdbc.queryForList(sql, new ArrayList<SqlStatementVisitor>());

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
            throw new UnsupportedChangeException("Failed to find foreign keys for table: " + getBaseTableName(), e);
        }
    }
}
