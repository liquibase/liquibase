package liquibase.database.statement.generator;

import liquibase.database.*;
import liquibase.database.statement.AddAutoIncrementStatement;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;
import liquibase.exception.JDBCException;

public class AddAutoIncrementGenerator implements SqlGenerator<AddAutoIncrementStatement> {

    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(AddAutoIncrementStatement statement, Database database) {
        return (database.supportsAutoIncrement()
                && !(database instanceof DerbyDatabase)
                && !(database instanceof HsqlDatabase));
    }

    public GeneratorValidationErrors validate(AddAutoIncrementStatement addAutoIncrementStatement, Database database) {
        return new GeneratorValidationErrors();
    }

    public Sql[] generateSql(AddAutoIncrementStatement statement, Database database) throws JDBCException {
        String sql = "ALTER TABLE "
                + database.escapeTableName(statement.getSchemaName(), statement.getTableName())
                + " MODIFY " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName())
                + " " + database.getColumnType(statement.getColumnDataType(), true)
                + " AUTO_INCREMENT";
        return new Sql[] {
                new UnparsedSql(sql)
        };
    }
}
