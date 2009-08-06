package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.GetViewDefinitionStatement;

public class GetViewDefinitionGeneratorMSSQL extends GetViewDefinitionGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(GetViewDefinitionStatement statement, Database database) {
        return database instanceof MSSQLDatabase;
    }

    @Override
    public Sql[] generateSql(GetViewDefinitionStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        try {
            String sql = "select view_definition from INFORMATION_SCHEMA.VIEWS where upper(table_name)='" + statement.getViewName().toUpperCase() + "'";
//        if (StringUtils.trimToNull(schemaName) != null) {
            sql += " and table_schema='" + database.convertRequestedSchemaToSchema(statement.getSchemaName()) + "'";
            sql += " and table_catalog='" + database.getDefaultCatalogName() + "'";
//        }

//        log.info("GetViewDefinitionSQL: "+sql);

            return new Sql[]{
                    new UnparsedSql(sql)
            };
        } catch (DatabaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }
}