package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.CacheDatabase;
import liquibase.database.core.DB2Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.FirebirdDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.exception.ValidationErrors;
import liquibase.informix.sqlgenerator.core.InformixCreateViewGenerator;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateViewStatement;

public class CreateViewGenerator extends AbstractSqlGenerator<CreateViewStatement> {

    public ValidationErrors validate(CreateViewStatement createViewStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
    	
    	if (database instanceof InformixDatabase) {
    		return new InformixCreateViewGenerator().validate(createViewStatement, database, sqlGeneratorChain);
    	}
    	
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("viewName", createViewStatement.getViewName());
        validationErrors.checkRequiredField("selectQuery", createViewStatement.getSelectQuery());

        if (createViewStatement.isReplaceIfExists()) {
            validationErrors.checkDisallowedField("replaceIfExists", createViewStatement.isReplaceIfExists(), database, HsqlDatabase.class, H2Database.class, DB2Database.class, CacheDatabase.class, MSSQLDatabase.class, DerbyDatabase.class, SybaseASADatabase.class, InformixDatabase.class);
        }

        return validationErrors;
    }

    public Sql[] generateSql(CreateViewStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
    	
    	if (database instanceof InformixDatabase) {
    		return new InformixCreateViewGenerator().generateSql(statement, database, sqlGeneratorChain);
    	}
    	
        String createClause;

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
        } else {
            createClause = "CREATE " + (statement.isReplaceIfExists() ? "OR REPLACE " : "") + "VIEW";
        }

        return new Sql[]{
                new UnparsedSql(createClause + " " + database.escapeViewName(statement.getCatalogName(), statement.getSchemaName(), statement.getViewName()) + " AS " + statement.getSelectQuery())
        };
    }
}
