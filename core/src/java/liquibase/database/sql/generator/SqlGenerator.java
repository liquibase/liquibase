package liquibase.database.sql.generator;

import liquibase.database.Database;
import liquibase.database.sql.SqlStatement;
import liquibase.exception.JDBCException;
import liquibase.exception.LiquibaseException;

public interface SqlGenerator {

    public static final int APPLICABILITY_NOT = -1;
    public static final int APPLICABILITY_DEFAULT = 1;
    public static final int APPLICABILITY_DATABASE_SPECIFIC = 5;

    public int getApplicability(SqlStatement statement, Database database);

    public String[] generateSql(SqlStatement statement, Database database) throws LiquibaseException;
}
