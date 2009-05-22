package liquibase.sqlgenerator;

import liquibase.statement.GetViewDefinitionStatement;
import liquibase.database.Database;
import liquibase.database.DB2Database;
import liquibase.database.FirebirdDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.exception.JDBCException;
import liquibase.exception.UnexpectedLiquibaseException;

public class GetViewDefinitionGeneratorFirebird extends GetViewDefinitionGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(GetViewDefinitionStatement statement, Database database) {
        return database instanceof FirebirdDatabase;
    }

    @Override
    public Sql[] generateSql(GetViewDefinitionStatement statement, Database database) {
        return new Sql[] {
                new UnparsedSql("select rdb$view_source from rdb$relations where upper(rdb$relation_name)='" + statement.getViewName() + "'")
        };
    }
}