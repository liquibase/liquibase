package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.InformixDatabase;
import liquibase.database.MSSQLDatabase;
import liquibase.database.SQLiteDatabase;
import liquibase.database.SybaseASADatabase;
import liquibase.database.SybaseDatabase;
import liquibase.statement.AddUniqueConstraintStatement;
import liquibase.sqlgenerator.core.AddUniqueConstraintGenerator;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.sqlgenerator.SqlGenerator;

public class AddUniqueConstraintGeneratorTest extends AbstractSqlGeneratorTest<AddUniqueConstraintStatement> {
    protected static final String TABLE_NAME = "AddUQTest";
    protected static final String COLUMN_NAME = "colToMakeUQ";
    protected static final String CONSTRAINT_NAME = "UQ_TEST";

    public AddUniqueConstraintGeneratorTest() throws Exception {
        this(new AddUniqueConstraintGenerator());
    }

    public AddUniqueConstraintGeneratorTest(SqlGenerator<AddUniqueConstraintStatement> generatorUnderTest) throws Exception {
        super(generatorUnderTest);
    }



    @Override
    protected AddUniqueConstraintStatement createSampleSqlStatement() {
        return new AddUniqueConstraintStatement(null, TABLE_NAME, COLUMN_NAME, null);
    }

    @Override
    protected boolean shouldBeImplementation(Database database) {
        return !(database instanceof SQLiteDatabase)
                && !(database instanceof MSSQLDatabase)
                && !(database instanceof SybaseDatabase)
                && !(database instanceof SybaseASADatabase)
                && !(database instanceof InformixDatabase)
        ;
    }   
}
