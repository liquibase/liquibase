package liquibase.sqlgenerator;

import liquibase.database.*;
import liquibase.statement.AddUniqueConstraintStatement;
import liquibase.statement.CreateTableStatement;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.SqlStatement;
import liquibase.test.TestContext;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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
                ;
    }   
}
