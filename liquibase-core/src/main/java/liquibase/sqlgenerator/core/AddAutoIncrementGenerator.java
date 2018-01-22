package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddAutoIncrementStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

public class AddAutoIncrementGenerator extends AbstractSqlGenerator<AddAutoIncrementStatement> {

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public boolean supports(AddAutoIncrementStatement statement, Database database) {
        return (database.supportsAutoIncrement()
                && !(database instanceof DerbyDatabase)
                && !(database instanceof MSSQLDatabase)
                && !(database instanceof HsqlDatabase)
                && !(database instanceof H2Database)
                && !(database instanceof OracleDatabase));
    }

    @Override
    public ValidationErrors validate(
            AddAutoIncrementStatement statement,
            Database database,
            SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("columnName", statement.getColumnName());
        validationErrors.checkRequiredField("tableName", statement.getTableName());
        validationErrors.checkRequiredField("columnDataType", statement.getColumnDataType());

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(
            AddAutoIncrementStatement statement,
            Database database,
            SqlGeneratorChain sqlGeneratorChain) {
    	String sql;
    	if (database instanceof SybaseASADatabase) {
            sql = "ALTER TABLE " +
                database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(),
                    statement.getTableName()) +
                " ALTER " +
                database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement
                    .getTableName(), statement.getColumnName()) +
                " SET " +
                database.getAutoIncrementClause(statement.getStartWith(), statement.getIncrementBy());
    	} else {
            sql = "ALTER TABLE " +
                database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement
                    .getTableName()) +
                " MODIFY " +
                database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement
                    .getTableName(), statement.getColumnName()) +
                " " +
                DataTypeFactory.getInstance().fromDescription(statement.getColumnDataType() +
                    "{autoIncrement:true}", database).toDatabaseDataType(database) +
                " " +
                database.getAutoIncrementClause(statement.getStartWith(), statement.getIncrementBy());
    	}
        return new Sql[]{
            new UnparsedSql(sql, getAffectedColumn(statement))
        };
    }

    protected Column getAffectedColumn(AddAutoIncrementStatement statement) {
        return new Column()
            .setRelation(new Table().setName(statement.getTableName()).setSchema(
                new Schema(statement.getCatalogName(), statement.getSchemaName())))
            .setName(statement.getColumnName());
    }
}
