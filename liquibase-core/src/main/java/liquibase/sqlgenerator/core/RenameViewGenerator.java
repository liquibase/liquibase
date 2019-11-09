package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.RenameViewStatement;
import liquibase.structure.core.Relation;
import liquibase.structure.core.View;

public class RenameViewGenerator extends AbstractSqlGenerator<RenameViewStatement> {

    @Override
    public boolean supports(RenameViewStatement statement, Database database) {
        return !((database instanceof DerbyDatabase) || (database instanceof HsqlDatabase) || (database instanceof
            H2Database) || (database instanceof AbstractDb2Database) || (database instanceof FirebirdDatabase) || (database
            instanceof InformixDatabase) || (database instanceof SybaseASADatabase));
    }

    @Override
    public ValidationErrors validate(RenameViewStatement renameViewStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("oldViewName", renameViewStatement.getOldViewName());
        validationErrors.checkRequiredField("newViewName", renameViewStatement.getNewViewName());

        validationErrors.checkDisallowedField("schemaName", renameViewStatement.getSchemaName(), database, OracleDatabase.class);

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(RenameViewStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String sql;

        if (database instanceof MSSQLDatabase) {
            sql = "exec sp_rename '" + database.escapeViewName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldViewName()) + "', '" + statement.getNewViewName() + '\'';
        } else if (database instanceof MySQLDatabase) {
            sql = "RENAME TABLE " + database.escapeViewName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldViewName()) + " TO " + database.escapeViewName(statement.getCatalogName(), statement.getSchemaName(), statement.getNewViewName());
        } else if (database instanceof PostgresDatabase) {
            sql = "ALTER TABLE " + database.escapeViewName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldViewName()) + " RENAME TO " + database.escapeObjectName(statement.getNewViewName(), View.class);
        } else if (database instanceof OracleDatabase) {
            sql = "RENAME " + database.escapeObjectName(statement.getOldViewName(), View.class) + " TO " + database.escapeObjectName(statement.getNewViewName(), View.class);
        } else {
            sql = "RENAME " + database.escapeViewName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldViewName()) + " TO " + database.escapeObjectName(statement.getNewViewName(), View.class);
        }

        return new Sql[]{
                new UnparsedSql(sql,
                        getAffectedOldView(statement),
                        getAffectedNewView(statement)
                )
        };
    }

    protected Relation getAffectedNewView(RenameViewStatement statement) {
        return new View().setName(statement.getNewViewName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }

    protected Relation getAffectedOldView(RenameViewStatement statement) {
        return new View().setName(statement.getOldViewName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }
}
