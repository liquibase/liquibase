package liquibase.database.statement.generator;

import liquibase.database.Database;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.statement.SqlStatement;
import liquibase.database.statement.syntax.Sql;
import liquibase.exception.LiquibaseException;
import liquibase.exception.JDBCException;

import java.util.Collection;

public interface SqlGenerator<StatementType extends SqlStatement> {

    public static final int SPECIALIZATION_LEVEL_DEFAULT = 1;
    public static final int SPECIALIZATION_LEVEL_DATABASE_SPECIFIC = 5;

    public int getSpecializationLevel();

    public boolean isValidGenerator(StatementType statement, Database database);

    public GeneratorValidationErrors validate(StatementType statementType, Database database);

    public Sql[] generateSql(StatementType statement, Database database);
}
