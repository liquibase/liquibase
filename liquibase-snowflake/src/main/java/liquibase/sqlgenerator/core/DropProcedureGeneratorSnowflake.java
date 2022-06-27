package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropProcedureStatement;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;

public class DropProcedureGeneratorSnowflake extends DropProcedureGenerator {

    @Override
    public boolean supports(DropProcedureStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public Sql[] generateSql(DropProcedureStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        // we shouldn't escape procedure name for Snowflake
        // DROP PROCEDURE "PUBLIC".proc3() -- works
        // DROP PROCEDURE proc3() -- works
        // DROP PROCEDURE "PUBLIC"."proc3()" -- default core implementation, doesn't work
        // DROP PROCEDURE "proc3()" -- doesn't work

        if (statement.getCatalogName() != null) {
            return new Sql[]{new UnparsedSql("DROP PROCEDURE " + database.escapeObjectName(statement.getCatalogName(), Catalog.class) + statement.getProcedureName())};
        } else if (statement.getSchemaName() != null) {
            return new Sql[]{new UnparsedSql("DROP PROCEDURE " + database.escapeObjectName(statement.getSchemaName(), Schema.class) + statement.getProcedureName())};
        } else {
            return new Sql[]{new UnparsedSql("DROP PROCEDURE " + statement.getProcedureName())};
        }
    }
}
