package liquibase.actionlogic.core;

import liquibase.actionlogic.core.AddUniqueConstraintLogic;
import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.statement.core.AddUniqueConstraintStatement;

public class AddUniqueConstraintLogicTest extends AbstractSqlGeneratorTest<AddUniqueConstraintStatement> {
    protected static final String TABLE_NAME = "AddUQTest";
    protected static final String COLUMN_NAME = "colToMakeUQ";
    protected static final String CONSTRAINT_NAME = "UQ_TEST";

    public AddUniqueConstraintLogicTest() throws Exception {
        this(new AddUniqueConstraintLogic());
    }

    protected AddUniqueConstraintLogicTest(SqlGenerator<AddUniqueConstraintStatement> generatorUnderTest) throws Exception {
        super(generatorUnderTest);
    }



    @Override
    protected AddUniqueConstraintStatement createSampleSqlStatement() {
        return new AddUniqueConstraintStatement(null, null, TABLE_NAME, new ColumnConfig[] {new ColumnConfig().setName(COLUMN_NAME)}, null);
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
