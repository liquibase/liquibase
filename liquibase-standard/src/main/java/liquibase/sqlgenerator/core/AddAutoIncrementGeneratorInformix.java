package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddAutoIncrementStatement;

public class AddAutoIncrementGeneratorInformix extends AddAutoIncrementGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddAutoIncrementStatement statement, Database database) {
        return database instanceof InformixDatabase;
    }

    @Override
    public ValidationErrors validate(
            AddAutoIncrementStatement addAutoIncrementStatement,
            Database database,
            SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = super.validate(
            addAutoIncrementStatement, database, sqlGeneratorChain);

        validationErrors.checkRequiredField(
            "columnDataType", addAutoIncrementStatement.getColumnDataType());

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(
            AddAutoIncrementStatement statement,
            Database database,
            SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[]{
            new UnparsedSql(
                "ALTER TABLE "
                    + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
                    + " MODIFY "
                    + database.escapeColumnName(
                        statement.getCatalogName(),
                        statement.getSchemaName(),
                        statement.getTableName(),
                        statement.getColumnName())
                    + " "
                    + DataTypeFactory.getInstance().fromDescription(statement.getColumnDataType() + "{autoIncrement:true}", database).toDatabaseDataType(database),
                getAffectedColumn(statement))
        };
    }
}

