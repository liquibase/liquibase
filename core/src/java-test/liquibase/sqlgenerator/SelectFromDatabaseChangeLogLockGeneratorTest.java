package liquibase.sqlgenerator;

import liquibase.database.Database;
import liquibase.database.PostgresDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.database.MSSQLDatabase;
import liquibase.statement.*;

import java.util.List;
import java.util.Arrays;

import org.junit.Test;

public class SelectFromDatabaseChangeLogLockGeneratorTest<T extends SelectFromDatabaseChangeLogLockStatement> extends AbstractSqlGeneratorTest<T> {
    public SelectFromDatabaseChangeLogLockGeneratorTest() throws Exception {
        super(new SelectFromDatabaseChangeLogLockGenerator());
    }

    @Override
    protected T createSampleSqlStatement() {
        return (T) new SelectFromDatabaseChangeLogLockStatement("LOCKED");
    }
}
