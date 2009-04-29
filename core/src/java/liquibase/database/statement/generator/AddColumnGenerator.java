package liquibase.database.statement.generator;

import liquibase.database.*;
import liquibase.database.structure.Column;
import liquibase.database.structure.Table;
import liquibase.database.statement.AddColumnStatement;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;

public class AddColumnGenerator implements SqlGenerator<AddColumnStatement> {
    
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(AddColumnStatement statement, Database database) {
        return true;
    }

    public GeneratorValidationErrors validate(AddColumnStatement statement, Database database) {
        GeneratorValidationErrors validationErrors = new GeneratorValidationErrors();

        if (statement.isPrimaryKey() && (database instanceof CacheDatabase
                || database instanceof H2Database
                || database instanceof DB2Database
                || database instanceof DerbyDatabase
                || database instanceof SQLiteDatabase)) {
            validationErrors.addError("Cannot add a primary key column");
        }

        if (database instanceof MySQLDatabase && statement.isAutoIncrement() && !statement.isPrimaryKey()) {
            validationErrors.addError("Cannot add a non-primary key identity column");
        }
        return validationErrors;
    }

    public Sql[] generateSql(AddColumnStatement statement, Database database) {

        String alterTable = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " ADD " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + database.getColumnType(statement.getColumnType(), statement.isAutoIncrement());

        if (statement.isAutoIncrement()) {
            alterTable += " " + database.getAutoIncrementClause();
        }

        if (!statement.isNullable()) {
            alterTable += " NOT NULL";
        } else {
            if (database instanceof SybaseDatabase || database instanceof SybaseASADatabase) {
                alterTable += " NULL";
            }
        }

        if (statement.isPrimaryKey()) {
            alterTable += " PRIMARY KEY";
        }

        alterTable += getDefaultClause(statement, database);

        return new Sql[]{
                new UnparsedSql(alterTable, new Column()
                        .setTable(new Table(statement.getTableName()).setSchema(statement.getSchemaName()))
                        .setName(statement.getColumnName()))
        };
    }

    private String getDefaultClause(AddColumnStatement statement, Database database) {
        String clause = "";
        if (statement.getDefaultValue() != null) {
            if (database instanceof MSSQLDatabase) {
                clause += " CONSTRAINT " + ((MSSQLDatabase) database).generateDefaultConstraintName(statement.getTableName(), statement.getColumnName());
            }
            clause += " DEFAULT " + database.convertJavaObjectToString(statement.getDefaultValue());
        }
        return clause;
    }

}
