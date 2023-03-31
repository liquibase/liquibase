package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.GetViewDefinitionStatement;

public class GetViewDefinitionGeneratorInformix extends GetViewDefinitionGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(GetViewDefinitionStatement statement, Database database) {
        return database instanceof InformixDatabase;
    }

    @Override
    public Sql[] generateSql(GetViewDefinitionStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        // TODO owner is schemaName ?
        // view definition is distributed over multiple rows, each 64 chars
    	// see InformixDatabase.getViewDefinition
        return new Sql[]{
                new UnparsedSql("select v.viewtext from sysviews v, systables t where t.tabname = '" + statement.getViewName() + "' and v.tabid = t.tabid and t.tabtype = 'V' order by v.seqno")
        };
    }
}