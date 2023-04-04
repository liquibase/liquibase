package liquibase.sqlgenerator.core;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.core.AddDefaultValueStatement;

public class AddDefaultValueGeneratorMySQL extends AddDefaultValueGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddDefaultValueStatement statement, Database database) {
        return database instanceof MySQLDatabase;
    }

    @Override
    public ValidationErrors validate(AddDefaultValueStatement addDefaultValueStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors errors = super.validate(addDefaultValueStatement, database, sqlGeneratorChain);
        try {
            if (addDefaultValueStatement.getDefaultValue() instanceof DatabaseFunction && database.getDatabaseMajorVersion() < 8) {
                errors.addError("This version of mysql does not support non-literal default values");
            }
        }
        catch (DatabaseException e){
            Scope.getCurrentScope().getLog(getClass()).fine("Can't get default value");
        }
        return errors;
    }
    @Override
    public Sql[] generateSql(AddDefaultValueStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        Object defaultValue = statement.getDefaultValue();
        String finalDefaultValue;
        if (defaultValue instanceof DatabaseFunction) {
            finalDefaultValue = "("+defaultValue+")";
            if (finalDefaultValue.startsWith("((")) {
                finalDefaultValue = defaultValue.toString();
            }
        } else {
            finalDefaultValue =  DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database);
        }
        return new Sql[]{
                new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ALTER " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " SET DEFAULT " + finalDefaultValue,
                        getAffectedColumn(statement))
        };
    }
}