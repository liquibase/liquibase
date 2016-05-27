package liquibase.sqlgenerator.core;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.DB2Database.DataServerType;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.GetViewDefinitionStatement;

public class GetViewDefinitionGeneratorDB2 extends GetViewDefinitionGenerator {
    @Override
    public int getPriority() {
        return SqlGenerator.PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(GetViewDefinitionStatement statement, Database database) {
        return database instanceof DB2Database;
    }

    @Override
    public Sql[] generateSql(GetViewDefinitionStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        CatalogAndSchema schema = new CatalogAndSchema(statement.getCatalogName(), statement.getSchemaName()).customize(database);

        if (((DB2Database)database).getDataServerType() == DataServerType.DB2Z){
            return new Sql[] {
                new UnparsedSql("select cast(statement as varchar(32704)) as view_definition from SYSIBM.SYSVIEWS where NAME='" + statement.getViewName() + "' and CREATOR='" + schema.getSchemaName() + "'")
            };
        } else {  
            return new Sql[] {
                new UnparsedSql("select view_definition from SYSIBM.VIEWS where TABLE_NAME='" + statement.getViewName() + "' and TABLE_SCHEMA='" + schema.getSchemaName() + "'")
            };
        }
    }
}
