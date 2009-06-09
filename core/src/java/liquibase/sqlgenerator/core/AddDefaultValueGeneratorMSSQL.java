package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.structure.Column;
import liquibase.database.structure.Table;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.AddDefaultValueStatement;
import liquibase.sqlgenerator.SqlGeneratorChain;

public class AddDefaultValueGeneratorMSSQL extends AddDefaultValueGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddDefaultValueStatement statement, Database database) {
        return database instanceof MSSQLDatabase;
    }

    @Override
    public Sql[] generateSql(AddDefaultValueStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[] {
                new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " ADD CONSTRAINT " + ((MSSQLDatabase) database).generateDefaultConstraintName(statement.getTableName(), statement.getColumnName()) + " DEFAULT " + database.convertJavaObjectToString(statement.getDefaultValue()) + " FOR " + statement.getColumnName(),
                        new Column()
                        .setTable(new Table(statement.getTableName()).setSchema(statement.getSchemaName()))
                        .setName(statement.getColumnName()))
        };
    }
}