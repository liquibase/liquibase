package liquibase.sqlgenerator.core;

import liquibase.statement.GetViewDefinitionStatement;
import liquibase.database.Database;
import liquibase.database.DerbyDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.exception.JDBCException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.sqlgenerator.SqlGeneratorChain;

public class GetViewDefinitionGeneratorDerby extends GetViewDefinitionGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(GetViewDefinitionStatement statement, Database database) {
        return database instanceof DerbyDatabase;
    }

    @Override
    public Sql[] generateSql(GetViewDefinitionStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        try {
            return new Sql[] {
                    new UnparsedSql("select V.VIEWDEFINITION from SYS.SYSVIEWS V, SYS.SYSTABLES T, SYS.SYSSCHEMAS S WHERE  V.TABLEID=T.TABLEID AND T.SCHEMAID=S.SCHEMAID AND T.TABLETYPE='V' AND T.TABLENAME='" + statement.getViewName() + "' AND S.SCHEMANAME='"+database.convertRequestedSchemaToSchema(statement.getSchemaName())+"'")
            };
        } catch (JDBCException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }
}