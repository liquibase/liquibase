package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddDefaultValueStatement;

public class AddDefaultValueGeneratorInformix extends AddDefaultValueGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddDefaultValueStatement statement, Database database) {
        return database instanceof InformixDatabase;
    }

    @Override
    public ValidationErrors validate(AddDefaultValueStatement addDefaultValueStatement, Database database,
            SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = super.validate(addDefaultValueStatement, database, sqlGeneratorChain);
        if (addDefaultValueStatement.getColumnDataType() == null) {
            validationErrors.checkRequiredField("columnDataType", addDefaultValueStatement.getColumnDataType());
        }
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(AddDefaultValueStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {

        Object defaultValue = statement.getDefaultValue();
        StringBuffer sql = new StringBuffer("ALTER TABLE ");
        sql.append(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()));
        sql.append(" MODIFY (");
        sql.append(database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(),
                statement.getColumnName()));
        sql.append(" ");
        sql.append(DataTypeFactory.getInstance().fromDescription(statement.getColumnDataType(), database)
                .toDatabaseDataType(database));
        sql.append(" DEFAULT ");
        sql.append(DataTypeFactory.getInstance().fromObject(defaultValue, database)
                .objectToSql(defaultValue, database));
        sql.append(")");
        UnparsedSql unparsedSql = new UnparsedSql(sql.toString(), getAffectedColumn(statement));
        return new Sql[] { unparsedSql };
    }
}
