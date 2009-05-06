package liquibase.statement.generator;

import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.syntax.Sql;
import liquibase.exception.ValidationErrors;

public interface SqlGenerator<StatementType extends SqlStatement> {

    public static final int SPECIALIZATION_LEVEL_DEFAULT = 1;
    public static final int SPECIALIZATION_LEVEL_DATABASE_SPECIFIC = 5;

    public int getSpecializationLevel();

    public boolean isValidGenerator(StatementType statement, Database database);

    public ValidationErrors validate(StatementType statementType, Database database);

    public Sql[] generateSql(StatementType statement, Database database);
}
