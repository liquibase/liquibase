package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.structure.Column;
import liquibase.database.structure.Schema;
import liquibase.database.structure.Table;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddAutoIncrementStatement;

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
                && !(database instanceof H2Database));
    }

    public ValidationErrors validate(
    		AddAutoIncrementStatement statement,
    		Database database,
    		SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("columnName", statement.getColumnName());
        validationErrors.checkRequiredField("tableName", statement.getTableName());

        return validationErrors;
    }

    public Sql[] generateSql(
    		AddAutoIncrementStatement statement,
    		Database database,
    		SqlGeneratorChain sqlGeneratorChain) {
        String sql = "ALTER TABLE "
            + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
            + " MODIFY "
            + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName())
            + " "
            + DataTypeFactory.getInstance().fromDescription(statement.getColumnDataType() + "{autoIncrement:true}")
            + " " 
            + database.getAutoIncrementClause(statement.getStartWith(), statement.getIncrementBy());

        return new Sql[]{
            new UnparsedSql(sql, new Column()
                .setRelation(new Table(statement.getTableName()).setSchema(new Schema(statement.getCatalogName(), statement.getSchemaName())))
                .setName(statement.getColumnName()))
        };
    }
}
