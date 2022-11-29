package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropViewStatement;
import liquibase.structure.core.Relation;
import liquibase.structure.core.View;
import liquibase.util.ObjectUtil;

public class DropViewGenerator extends AbstractSqlGenerator<DropViewStatement> {

    @Override
    public ValidationErrors validate(DropViewStatement dropViewStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("viewName", dropViewStatement.getViewName());

        validationErrors.checkDisallowedField("ifExists", dropViewStatement.isIfExists(), database, OracleDatabase.class);
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DropViewStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        final String command = "DROP VIEW " + (ObjectUtil.defaultIfNull(statement.isIfExists(), false) ? "IF EXISTS " : "");
        return new Sql[] {
                new UnparsedSql(command + database.escapeViewName(statement.getCatalogName(), statement.getSchemaName(), statement.getViewName()), getAffectedView(statement))
        };
    }

    protected Relation getAffectedView(DropViewStatement statement) {
        return new View().setName(statement.getViewName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }
}
