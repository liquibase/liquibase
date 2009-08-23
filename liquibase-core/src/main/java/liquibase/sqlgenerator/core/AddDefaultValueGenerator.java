package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.database.structure.Column;
import liquibase.database.structure.Table;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddDefaultValueStatement;

public class AddDefaultValueGenerator implements SqlGenerator<AddDefaultValueStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(AddDefaultValueStatement statement, Database database) {
        return true;
    }

    public ValidationErrors validate(AddDefaultValueStatement addDefaultValueStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("defaultValue", addDefaultValueStatement.getDefaultValue());
        validationErrors.checkRequiredField("columnName", addDefaultValueStatement.getColumnName());
        validationErrors.checkRequiredField("tableName", addDefaultValueStatement.getTableName());
        return validationErrors;
    }

    public Sql[] generateSql(AddDefaultValueStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        Object defaultValue = statement.getDefaultValue();
        return new Sql[]{
                new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN  " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " SET DEFAULT " + TypeConverterFactory.getInstance().findTypeConverter(database).getDataType(defaultValue).convertObjectToString(defaultValue, database),
                        new Column()
                                .setTable(new Table(statement.getTableName()).setSchema(statement.getSchemaName()))
                                .setName(statement.getColumnName()))
        };
    }
}
