package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.statement.core.AddUniqueConstraintStatement;

public class AddUniqueConstraintGeneratorTest extends AbstractSqlGeneratorTest<AddUniqueConstraintStatement> {
    protected static final String TABLE_NAME = "AddUQTest";
    protected static final String COLUMN_NAME = "colToMakeUQ";
    protected static final String CONSTRAINT_NAME = "UQ_TEST";

    public AddUniqueConstraintGeneratorTest() throws Exception {
        this(new AddUniqueConstraintGenerator());
    }

    protected AddUniqueConstraintGeneratorTest(SqlGenerator<AddUniqueConstraintStatement> generatorUnderTest) throws Exception {
        super(generatorUnderTest);
    }



    @Override
    protected AddUniqueConstraintStatement createSampleSqlStatement() {
        return new AddUniqueConstraintStatement(null, null, null, TABLE_NAME, COLUMN_NAME);
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
