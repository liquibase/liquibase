package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropPrimaryKeyStatement;
import liquibase.structure.core.Index;
import liquibase.structure.core.PrimaryKey;
import lombok.Setter;

import java.util.List;

/**
 * Removes an existing primary key.
 */
@DatabaseChange(name = "dropPrimaryKey", description = "Drops an existing primary key", priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "primaryKey")
@Setter
public class DropPrimaryKeyChange extends AbstractChange {
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String constraintName;
    private Boolean dropIndex;

    @Override
    public boolean generateStatementsVolatile(Database database) {
        return database instanceof SQLiteDatabase;
    }

    @DatabaseChangeProperty(mustEqualExisting ="primaryKey.catalog", description = "Name of the database catalog")
    public String getCatalogName() {
        return catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="primaryKey.schema", description = "Name of the database schema")
    public String getSchemaName() {
        return schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "primaryKey.table", description = "Name of the table to drop the primary key from")
    public String getTableName() {
        return tableName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "primaryKey", description = "Name of the primary key to drop")
    public String getConstraintName() {
        return constraintName;
    }

    @DatabaseChangeProperty(description = "Whether to drop the index associated with the primary key")
    public Boolean getDropIndex() {
        return dropIndex;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {

        if (database instanceof SQLiteDatabase) {
    		// return special statements for SQLite databases
    		return generateStatementsForSQLiteDatabase(database);
        }

        DropPrimaryKeyStatement statement = new DropPrimaryKeyStatement(getCatalogName(), getSchemaName(), getTableName(), getConstraintName());
        statement.setDropIndex(this.dropIndex);
        return new SqlStatement[]{
                statement,
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            return new ChangeStatus().assertComplete(!SnapshotGeneratorFactory.getInstance().has(new PrimaryKey(getConstraintName(), getCatalogName(), getSchemaName(), getTableName()), database), "Primary key exists");
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }

    }

    private SqlStatement[] generateStatementsForSQLiteDatabase(Database database) {
        SqlStatement[] sqlStatements = null;

        // Since SQLite does not support a drop column statement, use alter table visitor to copy the table
        // and disabling the primary key constraint from the column
        SQLiteDatabase.AlterTableVisitor alterTableVisitor = new SQLiteDatabase.AlterTableVisitor() {
            @Override
            public ColumnConfig[] getColumnsToAdd() {
                return new ColumnConfig[0];
            }

            @Override
            public boolean copyThisColumn(ColumnConfig column) {
                // toggle the primary key off
                if (column.getConstraints().isPrimaryKey()) {
                    column.getConstraints().setPrimaryKey(false);
                }
                return true;
            }

            @Override
            public boolean createThisColumn(ColumnConfig column) {
                // toggle the primary key off
                if (column.getConstraints().isPrimaryKey()) {
                    column.getConstraints().setPrimaryKey(false);
                }
                return true;
            }

            @Override
            public boolean createThisIndex(Index index) {
                return true;
            }
        };
        List<SqlStatement> statements;
        try {
            statements = SQLiteDatabase.getAlterTableStatements(alterTableVisitor, database, getCatalogName(), getSchemaName(), getTableName());
        } catch (DatabaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }
        if (statements.size() > 0) {
            sqlStatements = new SqlStatement[statements.size()];
            return statements.toArray(sqlStatements);
        }
        return sqlStatements;
    }

    @Override
    public String getConfirmationMessage() {
        return "Primary key dropped from "+getTableName();
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
