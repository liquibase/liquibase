package liquibase.sqlgenerator.core;

import liquibase.statement.core.GetViewDefinitionStatement;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.exception.JDBCException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.sqlgenerator.SqlGeneratorChain;

public class GetViewDefinitionGeneratorSybaseASA extends GetViewDefinitionGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(GetViewDefinitionStatement statement, Database database) {
        return database instanceof SybaseASADatabase;
    }

    @Override
    public Sql[] generateSql(GetViewDefinitionStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[]{
                new UnparsedSql("select viewtext from sysviews where upper(viewname)='" + statement.getViewName().toUpperCase() + "' and upper(vcreator) = '" + statement.getSchemaName().toUpperCase() + '\'')
        };
    }
}