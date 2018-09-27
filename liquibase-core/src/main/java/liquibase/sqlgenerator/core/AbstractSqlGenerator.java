package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.Warnings;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.SqlStatement;

import java.util.List;

public abstract class AbstractSqlGenerator<StatementType extends SqlStatement> implements SqlGenerator<StatementType> {

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public boolean generateStatementsIsVolatile(Database database) {
        return false;
    }

    @Override
    public boolean generateRollbackStatementsIsVolatile(Database database) {
        return false;
    }

    @Override
    public boolean supports(StatementType statement, Database database) {
        return true;
    }

    @Override
    public Warnings warn(StatementType statementType, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return sqlGeneratorChain.warn(statementType, database);
    }

    public boolean looksLikeFunctionCall(String value, Database database) {
        return value.startsWith("\"SYSIBM\"") || value.startsWith("to_date(") || value.equalsIgnoreCase(database.getCurrentDateTimeFunction());
    }



    /**
     * Convenience method for when the catalogName is set but we don't want to parse the body
     */
    public static void surroundWithCatalogSets(List<Sql> sql, String catalogName, Database database) {
        if (database instanceof MSSQLDatabase) {
            String defaultCatalogName = database.getDefaultCatalogName();
            sql.add(0, new UnparsedSql("USE [" + catalogName + "]"));
            sql.add(new UnparsedSql("USE [" + defaultCatalogName + "]"));
        }
    }

}
