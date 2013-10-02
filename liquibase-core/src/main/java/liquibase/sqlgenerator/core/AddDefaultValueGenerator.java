package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.structure.core.Schema;
import liquibase.datatype.DataTypeFactory;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddDefaultValueStatement;

public class AddDefaultValueGenerator extends AbstractSqlGenerator<AddDefaultValueStatement> {

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
                new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN  " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " SET DEFAULT " + DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database),
                        getAffectedColumn(statement))
        };
    }

    protected Column getAffectedColumn(AddDefaultValueStatement statement) {
        return new Column()
                .setRelation(new Table().setName(statement.getTableName()).setSchema(new Schema(statement.getCatalogName(), statement.getSchemaName())))
                .setName(statement.getColumnName());
    }
}
