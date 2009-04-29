package liquibase.database.statement.generator;

import liquibase.database.Database;
import liquibase.database.DB2Database;
import liquibase.database.structure.Column;
import liquibase.database.structure.Table;
import liquibase.database.statement.AddAutoIncrementStatement;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;

public class AddAutoIncrementGeneratorDB2 extends AddAutoIncrementGenerator {

    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DATABASE_SPECIFIC;
    }

    public boolean isValidGenerator(AddAutoIncrementStatement statement, Database database) {
        return database instanceof DB2Database;
    }

    public Sql[] generateSql(AddAutoIncrementStatement statement, Database database) {
        return new Sql[]{
                new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " SET GENERATED ALWAYS AS IDENTITY",
                        new Column()
                                .setTable(new Table(statement.getTableName()).setSchema(statement.getSchemaName()))
                                .setName(statement.getColumnName()))
        };
    }
}