package liquibase.sqlgenerator;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.database.structure.Column;
import liquibase.database.structure.Table;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.AddAutoIncrementStatement;

class AddAutoIncrementGeneratorDB2 extends AddAutoIncrementGenerator {

    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(AddAutoIncrementStatement statement, Database database) {
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