package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateViewStatement;

import java.util.ArrayList;
import java.util.List;

public class CreateViewGenerator extends AbstractSqlGenerator<CreateViewStatement> {

    public ValidationErrors validate(CreateViewStatement createViewStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("viewName", createViewStatement.getViewName());
        validationErrors.checkRequiredField("selectQuery", createViewStatement.getSelectQuery());

        if (createViewStatement.isReplaceIfExists()) {
            validationErrors.checkDisallowedField("replaceIfExists", createViewStatement.isReplaceIfExists(), database, HsqlDatabase.class, H2Database.class, DB2Database.class, CacheDatabase.class, DerbyDatabase.class, SybaseASADatabase.class, InformixDatabase.class);
        }

        return validationErrors;
    }

    public Sql[] generateSql(CreateViewStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String createClause;

        List<Sql> sql = new ArrayList<Sql>();

        if (database instanceof FirebirdDatabase) {
            if (statement.isReplaceIfExists()) {
                createClause = "RECREATE VIEW";
            } else {
                createClause = "RECREATE VIEW";
            }
        } else if (database instanceof SybaseASADatabase && statement.getSelectQuery().toLowerCase().startsWith("create view")) {
            // Sybase ASA saves view definitions with header.
            return new Sql[]{
                    new UnparsedSql(statement.getSelectQuery())
            };
        } else if (database instanceof MSSQLDatabase) {
            if (statement.isReplaceIfExists()) {
                //from http://stackoverflow.com/questions/163246/sql-server-equivalent-to-oracles-create-or-replace-view
                sql.add(new UnparsedSql("IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'["+statement.getSchemaName()+"].["+statement.getViewName()+"]'))\n" +
                        "    EXEC sp_executesql N'CREATE VIEW ["+statement.getSchemaName()+"].["+statement.getViewName()+"] AS SELECT ''This is a code stub which will be replaced by an Alter Statement'' as [code_stub]'"));
                createClause = "ALTER VIEW";
            } else {
                createClause = "CREATE VIEW";
            }
        } else {
            createClause = "CREATE " + (statement.isReplaceIfExists() ? "OR REPLACE " : "") + "VIEW";
        }

        return new Sql[]{
                new UnparsedSql(createClause + " " + database.escapeViewName(statement.getCatalogName(), statement.getSchemaName(), statement.getViewName()) + " AS " + statement.getSelectQuery())
        };
    }
}
