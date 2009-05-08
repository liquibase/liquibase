package liquibase.sqlgenerator;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.statement.SqlStatement;

interface SqlGenerator<StatementType extends SqlStatement> {

    public static final int PRIORITY_DEFAULT = 1;
    public static final int PRIORITY_DATABASE = 5;

    public int getPriority();

    public boolean supports(StatementType statement, Database database);

    public ValidationErrors validate(StatementType statementType, Database database);

    public Sql[] generateSql(StatementType statement, Database database);
}
