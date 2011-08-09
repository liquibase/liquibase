package liquibase.sqlgenerator.core;

import java.util.HashSet;

import liquibase.database.Database;
import liquibase.database.structure.Column;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.DatabaseSnapshotGenerator;
import liquibase.snapshot.DatabaseSnapshotGeneratorFactory;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.ModifyDataTypeStatement;

public class ModifyDataTypeGeneratorMySQL extends ModifyDataTypeGenerator {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
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
        DatabaseSnapshot snapshot = null;
        try {
            snapshot = generator.createSnapshot(database, statement.getSchemaName(), new HashSet<DiffStatusListener>());
        } catch (DatabaseException e) {
            throw new UnexpectedLiquibaseException("Error retrieving database snapshot", e);
        }

        Column col = null;
        try {
            col = snapshot.getColumn(statement.getTableName(), statement.getColumnName());
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
