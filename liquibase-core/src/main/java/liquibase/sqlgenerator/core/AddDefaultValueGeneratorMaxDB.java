package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.structure.Schema;
import liquibase.datatype.DataTypeFactory;
import liquibase.database.core.MaxDBDatabase;
import liquibase.database.structure.Column;
import liquibase.database.structure.Table;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddDefaultValueStatement;

public class AddDefaultValueGeneratorMaxDB extends AddDefaultValueGenerator {
    @Override
    public int getPriority() {
        return SqlGenerator.PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddDefaultValueStatement statement, Database database) {
        return database instanceof MaxDBDatabase;
    }

    @Override
    public Sql[] generateSql(AddDefaultValueStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        Object defaultValue = statement.getDefaultValue();
        return new Sql[]{
                new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " COLUMN  " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " ADD DEFAULT " + DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToString(defaultValue, database),
                        new Column()
                                .setRelation(new Table(statement.getTableName()).setSchema(new Schema(statement.getCatalogName(), statement.getSchemaName())))
                                .setName(statement.getColumnName()))
        };
    }
}