package liquibase.sqlgenerator.core;

import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.GetNextChangeSetSequenceValueStatement;
import liquibase.statement.core.SelectFromDatabaseChangeLogStatement;

public class GetNextChangeSetSequenceValueGenerator extends AbstractSqlGenerator<GetNextChangeSetSequenceValueStatement> {

    @Override
    public ValidationErrors validate(GetNextChangeSetSequenceValueStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Sql[] generateSql(GetNextChangeSetSequenceValueStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return SqlGeneratorFactory.getInstance().generateSql(new SelectFromDatabaseChangeLogStatement(new ColumnConfig().setName("MAX(ORDEREXECUTED)", true)), database);
    }
}
