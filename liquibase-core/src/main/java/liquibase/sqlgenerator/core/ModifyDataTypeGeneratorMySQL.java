package liquibase.sqlgenerator.core;

import java.util.HashSet;

import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.structure.Column;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.Warnings;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.DatabaseSnapshotGenerator;
import liquibase.snapshot.DatabaseSnapshotGeneratorFactory;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.ModifyDataTypeStatement;

public class ModifyDataTypeGeneratorMySQL<StatementType extends SqlStatement> extends ModifyDataTypeGenerator {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public Warnings warn(ModifyDataTypeStatement modifyDataTypeStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        Warnings warnings = super.warn(modifyDataTypeStatement, database, sqlGeneratorChain);

        if (database instanceof MySQLDatabase && !modifyDataTypeStatement.getNewDataType().toLowerCase().contains("varchar")) {
            warnings.addWarning("modifyDataType will lose primary key/autoincrement/not null settings for MySQL in updateSQL mode.  Use <sql> and re-specify all configuration if this is the case");
        }

        return warnings;
    }

    /** @{inheritDoc}. */
    @Override
    public boolean supports(final ModifyDataTypeStatement statement, final Database database)
    {
        return database instanceof MySQLDatabase;
    }

    /**
     * Generate MySQL-specific modify statement.
     *
     * @param statement Statement instance
     * @param database Database instance
     * @param sqlGeneratorChain SqlGeneratorChain instance
     * @return SQL statement array
     */
    public Sql[] generateSql(ModifyDataTypeStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {

        // Main alter statement
        String alterTable = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " MODIFY ";

        // Add column name
        alterTable += database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " ";

        // Add column type
        alterTable += TypeConverterFactory.getInstance().findTypeConverter(database).getDataType(statement.getNewDataType(), false);

        // Get column information to avoid inadvertently removing default value, null-ability, uniqueness, etc.
        DatabaseSnapshotGeneratorFactory factory = DatabaseSnapshotGeneratorFactory.getInstance();
        DatabaseSnapshotGenerator generator = factory.getGenerator(database);
        DatabaseSnapshot snapshot;
        try {
            snapshot = generator.createSnapshot(database, statement.getSchemaName(), new HashSet<DiffStatusListener>());
        } catch (DatabaseException e) {
            throw new UnexpectedLiquibaseException("Error retrieving database snapshot", e);
        }

        Column col;
        try {
            col = snapshot.getColumn(statement.getTableName(), statement.getColumnName());
            if (col == null) {
                // If column metadata can't be found, give up trying to maintain the existing column constraints and return
                return new Sql[]{new UnparsedSql(alterTable)};
            }
        } catch (NullPointerException e) {
            throw new UnexpectedLiquibaseException("Error retrieving database snapshot", e);
        }

        if (!col.isNullable()) {
            alterTable += " NOT NULL";
        }

        if (col.getDefaultValue() != null) {
            alterTable += " DEFAULT " + col.getDefaultValue();
        }

        if (col.isAutoIncrement()) {
            alterTable += " AUTO INCREMENT";
        }

        if (col.isUnique()) {
            alterTable += " UNIQUE";
        }

        if (col.isPrimaryKey()) {
            alterTable += " PRIMARY KEY";
        }

        return new Sql[]{new UnparsedSql(alterTable)};
    }
}
