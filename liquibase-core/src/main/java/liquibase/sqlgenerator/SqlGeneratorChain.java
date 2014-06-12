package liquibase.sqlgenerator;

import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.sql.Sql;
import liquibase.statement.SqlStatement;

public class SqlGeneratorChain {

    private ActionGeneratorChain delegate;

    public SqlGeneratorChain(ActionGeneratorChain actionGeneratorChain) {
        this.delegate = actionGeneratorChain;
    }

    public Sql[] generateSql(SqlStatement statement, Database database) {
        return (Sql[]) delegate.generateActions(statement, database);
    }

    public Warnings warn(SqlStatement statement, Database database) {
        return delegate.warn(statement, database);
    }

    public ValidationErrors validate(SqlStatement statement, Database database) {
        return delegate.validate(statement, database);
    }
}
