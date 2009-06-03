package liquibase.sqlgenerator.core;

import liquibase.statement.GetViewDefinitionStatement;
import liquibase.database.Database;
import liquibase.database.InformixDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;

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
    public Sql[] generateSql(GetViewDefinitionStatement statement, Database database) {
        // TODO owner is schemaName ?
        // view definition is distributed over multiple rows, each 64 chars

        return new Sql[]{
                new UnparsedSql("select v.viewtext from sysviews v, systables t where t.tabname = '" + statement.getViewName() + "' and v.tabid = t.tabid and t.tabtype = 'V' order by v.seqno")
        };
    }
}