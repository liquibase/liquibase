package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddAutoIncrementStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

/**
 * SQLite does not support this ALTER TABLE operation until now.
 * For more information see: http://www.sqlite.org/omitted.html.
 * This is a small work around...
 */
public class AddAutoIncrementGeneratorMySQL extends AddAutoIncrementGenerator {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddAutoIncrementStatement statement, Database database) {
        return database instanceof MySQLDatabase;
    }

    @Override
    public ValidationErrors validate(
            AddAutoIncrementStatement statement,
            Database database,
            SqlGeneratorChain sqlGeneratorChain) {

        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("tableName", statement.getTableName());
        validationErrors.checkRequiredField("startWith", statement.getStartWith());

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(final AddAutoIncrementStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
    	MySQLDatabase mysqlDatabase = (MySQLDatabase)database;

        String sql = "ALTER TABLE "
            + mysqlDatabase.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
            + " "
            + mysqlDatabase.getTableOptionAutoIncrementStartWithClause(statement.getStartWith());
        ;

        return new Sql[]{
            new UnparsedSql(sql, getAffectedTable(statement))
        };
    }

	private DatabaseObject getAffectedTable(AddAutoIncrementStatement statement) {
		return new Table().setName(statement.getTableName()).setSchema(new Schema(statement.getCatalogName(), statement.getSchemaName()));
	}
}