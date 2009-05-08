package liquibase.sqlgenerator;

import liquibase.database.Database;
import liquibase.database.DerbyDatabase;
import liquibase.database.structure.Column;
import liquibase.database.structure.Table;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.AddDefaultValueStatement;

public class AddDefaultValueGeneratorDerby extends AddDefaultValueGenerator {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DATABASE_SPECIFIC;
    }

    public boolean isValidGenerator(AddDefaultValueStatement statement, Database database) {
        return database instanceof DerbyDatabase;
    }

    public Sql[] generateSql(AddDefaultValueStatement statement, Database database) {
        return new Sql[]{
                new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN  " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " WITH DEFAULT " + database.convertJavaObjectToString(statement.getDefaultValue()),
                        new Column()
                                .setTable(new Table(statement.getTableName()).setSchema(statement.getSchemaName()))
                                .setName(statement.getColumnName()))
        };
    }
}