package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.structure.Column;
import liquibase.database.structure.Schema;
import liquibase.database.structure.Table;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddDefaultValueStatement;

public class AddDefaultValueGeneratorDerby extends AddDefaultValueGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddDefaultValueStatement statement, Database database) {
        return database instanceof DerbyDatabase;
    }

    @Override
    public Sql[] generateSql(AddDefaultValueStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        Object defaultValue = statement.getDefaultValue();
        return new Sql[]{
                new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN  " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " WITH DEFAULT " + database.getDataTypeFactory().fromObject(defaultValue, database).objectToString(defaultValue, database),
                        new Column()
                                .setRelation(new Table(statement.getTableName()).setSchema(new Schema(statement.getCatalogName(), statement.getSchemaName())))
                                .setName(statement.getColumnName()))
        };
    }
}