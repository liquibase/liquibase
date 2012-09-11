package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.structure.core.Schema;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.GetViewDefinitionStatement;

public class GetViewDefinitionGeneratorHsql extends GetViewDefinitionGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(GetViewDefinitionStatement statement, Database database) {
        return database instanceof HsqlDatabase;
    }

    @Override
    public Sql[] generateSql(GetViewDefinitionStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        Schema schema = database.correctSchema(new Schema(statement.getCatalogName(), statement.getSchemaName()));

        return new Sql[] {
                    new UnparsedSql("SELECT VIEW_DEFINITION FROM INFORMATION_SCHEMA.SYSTEM_VIEWS WHERE TABLE_NAME = '" + statement.getViewName() + "' AND TABLE_SCHEMA='" + schema.getName() + "'")
            };
    }
}