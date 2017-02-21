package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.structure.core.Schema;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import liquibase.database.core.MySQLDatabase;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
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
    	ValidationErrors validationErrors;
    	
    	validationErrors = super.validate(addDefaultValueStatement, database, sqlGeneratorChain);
    	
        validationErrors.checkRequiredField("columnDataType", StringUtils.trimToNull(addDefaultValueStatement.getColumnDataType()));

        return validationErrors;
    }
    
    @Override
    public Sql[] generateSql(AddDefaultValueStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        Object defaultValue = statement.getDefaultValue();
        return new Sql[]{
                new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " MODIFY COLUMN " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + DataTypeFactory.getInstance().fromDescription(statement.getColumnDataType(), database).toDatabaseDataType(database) + " DEFAULT " + DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database),
                       getAffectedColumn(statement))
        };
    }
}