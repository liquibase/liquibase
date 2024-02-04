package liquibase.sqlgenerator.core;

import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.statement.core.AddUniqueConstraintStatement;
import liquibase.test.TestContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AddUniqueConstraintGeneratorParameterizedTest {
    protected static final String TABLE_NAME = "AddUQTest";
    protected static final String COLUMN_NAME = "colToMakeUQ";
    protected static final String COLUMN_NAME2 = "colToMakeUQ2";
    protected static final String CONSTRAINT_NAME = "UQ_TEST";
    private static final String INDEX_NAME = "uqIndex";


    protected SqlGenerator<AddUniqueConstraintStatement> generatorUnderTest = new AddUniqueConstraintGenerator();


    public static Stream<Arguments> getAllDatabases() {
        return TestContext.getInstance().getAllDatabases().stream().map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("getAllDatabases")
    public void testUniqueConstraintUsingIndex(Database database) {
        AddUniqueConstraintStatement statement = new AddUniqueConstraintStatement(null, null, TABLE_NAME, new ColumnConfig[]{new ColumnConfig().setName(COLUMN_NAME), new ColumnConfig().setName(COLUMN_NAME2)}, CONSTRAINT_NAME);
        statement.setForIndexName(INDEX_NAME);
        if (database instanceof H2Database) {
            assertEquals("ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE (colToMakeUQ, colToMakeUQ2)", this.generatorUnderTest.generateSql(statement, database, null)[0].toSql(), "Failed for " + database.getShortName());
        } else if (database instanceof PostgresDatabase) {
            assertEquals("ALTER TABLE \"AddUQTest\" ADD CONSTRAINT UQ_TEST UNIQUE (\"colToMakeUQ\", \"colToMakeUQ2\") USING INDEX \"uqIndex\"", this.generatorUnderTest.generateSql(statement, database, null)[0].toSql(), "Failed for " + database.getShortName());
        } else {
            assertEquals("ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex", this.generatorUnderTest.generateSql(statement, database, null)[0].toSql(), "Failed for " + database.getShortName());
        }
    }

    @ParameterizedTest
    @MethodSource("getAllDatabases")
    public void testUniqueConstraintUsingIndexWithoutConstraintName(Database database) {
        AddUniqueConstraintStatement statement = new AddUniqueConstraintStatement(null, null, TABLE_NAME, new ColumnConfig[]{new ColumnConfig().setName(COLUMN_NAME), new ColumnConfig().setName(COLUMN_NAME2)}, null);
        statement.setForIndexName(INDEX_NAME);
        if (database instanceof H2Database) {
            assertEquals("ALTER TABLE AddUQTest ADD UNIQUE (colToMakeUQ, colToMakeUQ2)", this.generatorUnderTest.generateSql(statement, database, null)[0].toSql(), "Failed for " + database.getShortName());
        } else if (database instanceof PostgresDatabase) {
            assertEquals("ALTER TABLE \"AddUQTest\" ADD UNIQUE (\"colToMakeUQ\", \"colToMakeUQ2\") USING INDEX \"uqIndex\"", this.generatorUnderTest.generateSql(statement, database, null)[0].toSql(), "Failed for " + database.getShortName());
        } else {
            assertEquals("ALTER TABLE AddUQTest ADD UNIQUE (colToMakeUQ, colToMakeUQ2) USING INDEX uqIndex", this.generatorUnderTest.generateSql(statement, database, null)[0].toSql(), "Failed for " + database.getShortName());
        }
    }

    @ParameterizedTest
    @MethodSource("getAllDatabases")
    public void testUniqueConstraintClustered(Database database) {
        AddUniqueConstraintStatement statement = new AddUniqueConstraintStatement(null, null, TABLE_NAME, new ColumnConfig[]{new ColumnConfig().setName(COLUMN_NAME), new ColumnConfig().setName(COLUMN_NAME2)}, CONSTRAINT_NAME);
        statement.setClustered(true);
        if (database instanceof PostgresDatabase) {
            assertEquals("ALTER TABLE \"AddUQTest\" ADD CONSTRAINT UQ_TEST UNIQUE CLUSTERED (\"colToMakeUQ\", \"colToMakeUQ2\")", this.generatorUnderTest.generateSql(statement, database, null)[0].toSql(), "Failed for " + database.getShortName());
        } else {
            assertEquals("ALTER TABLE AddUQTest ADD CONSTRAINT UQ_TEST UNIQUE CLUSTERED (colToMakeUQ, colToMakeUQ2)", this.generatorUnderTest.generateSql(statement, database, null)[0].toSql(), "Failed for " + database.getShortName());
        }
    }
}
