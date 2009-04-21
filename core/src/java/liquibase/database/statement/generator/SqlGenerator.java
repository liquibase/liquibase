package liquibase.database.statement.generator;

import liquibase.database.Database;
import liquibase.database.statement.SqlStatement;
import liquibase.exception.LiquibaseException;

public interface SqlGenerator<StatementType extends SqlStatement> {

    public static final int SPECIALIZATION_LEVEL_DEFAULT = 1;
    public static final int SPECIALIZATION_LEVEL_DATABASE_SPECIFIC = 5;

    public int getSpecializationLevel();

    public boolean isValid(StatementType statement, Database database);

    public String[] generateSql(StatementType statement, Database database) throws LiquibaseException;
}
