package liquibase.sqlgenerator;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.statement.SqlStatement;

public interface SqlGenerator<StatementType extends SqlStatement> {

    public static final int SPECIALIZATION_LEVEL_DEFAULT = 1;
    public static final int SPECIALIZATION_LEVEL_DATABASE_SPECIFIC = 5;

    public int getSpecializationLevel();

    public boolean isValidGenerator(StatementType statement, Database database);

    public ValidationErrors validate(StatementType statementType, Database database);

    public Sql[] generateSql(StatementType statement, Database database);
}
