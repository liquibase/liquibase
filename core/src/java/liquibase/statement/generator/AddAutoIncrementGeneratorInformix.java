package liquibase.statement.generator;

import liquibase.database.Database;
import liquibase.database.InformixDatabase;
import liquibase.database.structure.Column;
import liquibase.database.structure.Table;
import liquibase.statement.AddAutoIncrementStatement;
import liquibase.statement.syntax.Sql;
import liquibase.statement.syntax.UnparsedSql;
import liquibase.exception.ValidationErrors;

public class AddAutoIncrementGeneratorInformix extends AddAutoIncrementGenerator {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DATABASE_SPECIFIC;
    }

    public boolean isValidGenerator(AddAutoIncrementStatement statement, Database database) {
        return database instanceof InformixDatabase;
    }

    @Override
    public ValidationErrors validate(AddAutoIncrementStatement addAutoIncrementStatement, Database database) {
        ValidationErrors validationErrors = super.validate(addAutoIncrementStatement, database);

        validationErrors.checkRequiredField("columnDataType", addAutoIncrementStatement.getColumnDataType());

        return validationErrors;
    }

    public Sql[] generateSql(AddAutoIncrementStatement statement, Database database) {
        return new Sql[]{
                new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " MODIFY " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + database.getColumnType(statement.getColumnDataType(), true),
                        new Column()
                                .setTable(new Table(statement.getTableName()).setSchema(statement.getSchemaName()))
                                .setName(statement.getColumnName()))
        };
    }
}

