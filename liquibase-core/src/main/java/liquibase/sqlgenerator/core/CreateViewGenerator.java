package liquibase.sqlgenerator.core;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.structure.core.Relation;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateViewStatement;
import liquibase.structure.core.View;

import java.util.ArrayList;
import java.util.List;

public class CreateViewGenerator extends AbstractSqlGenerator<CreateViewStatement> {

    @Override
    public ValidationErrors validate(CreateViewStatement createViewStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
    	
    	if (database instanceof InformixDatabase) {
    		return new CreateViewGeneratorInformix().validate(createViewStatement, database, sqlGeneratorChain);
    	}
    	
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("viewName", createViewStatement.getViewName());
        validationErrors.checkRequiredField("selectQuery", createViewStatement.getSelectQuery());

        if (createViewStatement.isReplaceIfExists()) {
            validationErrors.checkDisallowedField("replaceIfExists", createViewStatement.isReplaceIfExists(), database, HsqlDatabase.class, DB2Database.class, DerbyDatabase.class, SybaseASADatabase.class, InformixDatabase.class);
        }

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(CreateViewStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
    	
    	if (database instanceof InformixDatabase) {
    		return new CreateViewGeneratorInformix().generateSql(statement, database, sqlGeneratorChain);
    	}
    	
        String createClause;

        List<Sql> sql = new ArrayList<Sql>();

        if (statement.isFullDefinition()) {
            sql.add(new UnparsedSql(statement.getSelectQuery(), getAffectedView(statement)));
        } else {
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
                    CatalogAndSchema schema = new CatalogAndSchema(statement.getCatalogName(), statement.getSchemaName()).customize(database);
                    sql.add(new UnparsedSql("IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'["+ schema.getSchemaName() +"].["+statement.getViewName()+"]'))\n" +
                            "    EXEC sp_executesql N'CREATE VIEW ["+schema.getSchemaName()+"].["+statement.getViewName()+"] AS SELECT ''This is a code stub which will be replaced by an Alter Statement'' as [code_stub]'"));
                    createClause = "ALTER VIEW";
                } else {
                    createClause = "CREATE VIEW";
                }
            } else if (database instanceof PostgresDatabase) {
                if (statement.isReplaceIfExists()) {
                    sql.add(new UnparsedSql("DROP VIEW IF EXISTS "+database.escapeViewName(statement.getCatalogName(), statement.getSchemaName(), statement.getViewName())));
                }
                createClause = "CREATE VIEW";
            } else {
                createClause = "CREATE " + (statement.isReplaceIfExists() ? "OR REPLACE " : "") + "VIEW";
            }
            sql.add(new UnparsedSql(createClause + " " + database.escapeViewName(statement.getCatalogName(), statement.getSchemaName(), statement.getViewName()) + " AS " + statement.getSelectQuery(), getAffectedView(statement)));
        }

        return sql.toArray(new Sql[sql.size()]);
    }

    protected Relation getAffectedView(CreateViewStatement statement) {
        return new View().setName(statement.getViewName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }
}
