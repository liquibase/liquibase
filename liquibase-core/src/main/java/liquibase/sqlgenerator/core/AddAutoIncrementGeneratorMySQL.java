package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddAutoIncrementStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

public class AddAutoIncrementGeneratorMySQL extends AddAutoIncrementGenerator {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddAutoIncrementStatement statement, Database database) {
        return database instanceof MySQLDatabase;
    }

    @Override
    public Sql[] generateSql(final AddAutoIncrementStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {

        Sql[] sql = super.generateSql(statement, database, sqlGeneratorChain);

        if(statement.getStartWith() != null){
            MySQLDatabase mysqlDatabase = (MySQLDatabase)database;
            String alterTableSql = "ALTER TABLE "
                + mysqlDatabase.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
                + " "
                + mysqlDatabase.getTableOptionAutoIncrementStartWithClause(statement.getStartWith());

            sql = concact(sql, new UnparsedSql(alterTableSql, getAffectedTable(statement)));
        }

        return sql;
    }

    private Sql[] concact(Sql[] origSql, UnparsedSql unparsedSql) {
        Sql[] changedSql = new Sql[origSql.length+1];
        System.arraycopy(origSql, 0, changedSql, 0, origSql.length);
        changedSql[origSql.length] = unparsedSql;

        return changedSql;
    }

    private DatabaseObject getAffectedTable(AddAutoIncrementStatement statement) {
        return new Table().setName(statement.getTableName()).setSchema(new Schema(statement.getCatalogName(), statement.getSchemaName()));
    }
}