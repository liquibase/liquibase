package liquibase.change;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.serializer.string.StringChangeLogSerializer;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.RawSqlStatement;
import liquibase.statement.SqlStatement;
import liquibase.test.TestContext;
import liquibase.util.StreamUtil;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.junit.Assert.*;
import org.junit.Test;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.*;

/**
 * Base test class for changes
 */
public abstract class AbstractChangeTest {

    @Test
    public abstract void getRefactoringName() throws Exception;

    @Test
    public abstract void generateStatement() throws Exception;

    @Test
    public abstract void getConfirmationMessage() throws Exception;

    @Test
    public void generateCheckSum() throws Exception {
        Change change = createClassUnderTest();
        if (change == null) {
            return;
        }

        CheckSum checkSum = change.generateCheckSum();
        assertNotNull(checkSum);
        assertEquals(2, checkSum.getVersion());
        assertTrue(checkSum.toString().startsWith("2:"));

        Map<String, String> seenCheckSums = new HashMap<String, String>();
        for (Field field : change.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (field.getName().startsWith("$VR") || field.getName().equals("serialVersionUID")) {
                //code coverage related
            } else  if (String.class.isAssignableFrom(field.getType())) {
                field.set(change, "asdghasdgasdg");
                checkThatChecksumIsNew(change, seenCheckSums, field);
                field.set(change, "gsgasdgasdggasdg sdg a");
                checkThatChecksumIsNew(change, seenCheckSums, field);
            } else if (Boolean.class.isAssignableFrom(field.getType())) {
                field.set(change, Boolean.TRUE);
                checkThatChecksumIsNew(change, seenCheckSums, field);
                field.set(change, Boolean.FALSE);
                checkThatChecksumIsNew(change, seenCheckSums, field);
            } else if (Integer.class.isAssignableFrom(field.getType())) {
                field.set(change, 6532);
                checkThatChecksumIsNew(change, seenCheckSums, field);
                field.set(change, -52352);
                checkThatChecksumIsNew(change, seenCheckSums, field);
            } else if (List.class.isAssignableFrom(field.getType())) {
                ColumnConfig column1 = new ColumnConfig();
                ((List) field.get(change)).add(column1);

                column1.setName("ghsdgasdg");
                checkThatChecksumIsNew(change, seenCheckSums, field);

                column1.setType("gasdgasdg");
                checkThatChecksumIsNew(change, seenCheckSums, field);

                ColumnConfig column2 = new ColumnConfig();
                ((List) field.get(change)).add(column2);

                column2.setName("87682346asgasdg");
                checkThatChecksumIsNew(change, seenCheckSums, field);

            } else {
                throw new RuntimeException("Unknown field type: "+field.getType()+" for "+field.getName());
            }
        }
    }

    private Change createClassUnderTest() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        String className = getClass().getName().replaceAll("Test$", "");
        if (className.indexOf("Abstract") > 0) {
            return null;
        }

        return (Change) Class.forName(className).getConstructor().newInstance();
    }

    private void checkThatChecksumIsNew(Change change, Map<String, String> seenCheckSums, Field field) {
        String serialized = new StringChangeLogSerializer().serialize(change);

        CheckSum newCheckSum = change.generateCheckSum();
        if (seenCheckSums.containsKey(newCheckSum.toString())) {
            fail("generated duplicate checksum channging "+field.getName()+"\n"+serialized+"\nmatches\n"+seenCheckSums.get(newCheckSum.toString()));
        }
        seenCheckSums.put(newCheckSum.toString(), serialized);
    }

    @Test
    public void saveStatement() throws Exception {
        Change change = new AbstractChange("test", "Test Refactoring", ChangeMetaData.PRIORITY_DEFAULT) {
            public SqlStatement[] generateStatements(Database database) {
                return new SqlStatement[]{new RawSqlStatement("GENERATED STATEMENT")};
            }

            public String getConfirmationMessage() {
                return null;
            }
        };

        StringWriter stringWriter = new StringWriter();

        OracleDatabase database = new OracleDatabase();
        database.saveStatements(change, new ArrayList<SqlVisitor>(), stringWriter);

        assertEquals("GENERATED STATEMENT;" + StreamUtil.getLineSeparator() + StreamUtil.getLineSeparator(), stringWriter.getBuffer().toString());
    }

    @Test
    public void executeStatement() throws Exception {
        Change change = new AbstractChange("test", "Test Refactorign", ChangeMetaData.PRIORITY_DEFAULT) {
            public SqlStatement[] generateStatements(Database database) {
                return new SqlStatement[]{new RawSqlStatement("GENERATED STATEMENT;")};
            }

            public String getConfirmationMessage() {
                return null;
            }
        };

        Connection conn = createMock(Connection.class);
        Statement statement = createMock(Statement.class);
        conn.setAutoCommit(false);
        expect(conn.createStatement()).andReturn(statement);

        expect(statement.execute("GENERATED STATEMENT;")).andStubReturn(true);
        statement.close();
        expectLastCall();
        replay(conn);
        replay(statement);

        OracleDatabase database = new OracleDatabase();
        database.setConnection(conn);

        database.executeStatements(change, new ArrayList<SqlVisitor>());

        verify(conn);
        verify(statement);
    }

    protected void testChangeOnAllExcept(Change change, GenerateAllValidator validator, Class<? extends Database>... databases) throws Exception {
        List<Class<? extends Database>> databsesToRun = new ArrayList<Class<? extends Database>>();
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            List<Class<? extends Database>> databaseClasses = Arrays.asList(databases);
            if (!databaseClasses.contains(database.getClass())) {
                databsesToRun.add(database.getClass());
            }
        }

        testChange(change, validator, databsesToRun.toArray(new Class[databsesToRun.size()]));
    }

    protected void testChangeOnAll(Change change, GenerateAllValidator validator) throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            SqlStatement[] sqlStatements = change.generateStatements(database);
            try {
                validator.validate(sqlStatements, database);
            } catch (AssertionError e) {
                AssertionError error = new AssertionError("GenerateAllValidator failed for " + database.getTypeName() + ": " + e.getMessage());
                error.setStackTrace(e.getStackTrace());

                throw error;
            }
        }
    }

    protected void testChange(Change change, GenerateAllValidator validator, Class<? extends Database>... databases) throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            List<Class<? extends Database>> databaseClasses = Arrays.asList(databases);
            if (!databaseClasses.contains(database.getClass())) {
                continue;
            }

            SqlStatement[] sqlStatements = change.generateStatements(database);
            try {
                validator.validate(sqlStatements, database);
            } catch (AssertionError e) {
                AssertionError error = new AssertionError("GenerateAllValidator failed for " + database.getTypeName() + ": " + e.getMessage());
                error.setStackTrace(e.getStackTrace());

                throw error;
            }
        }
    }

    protected void testInverseOnAllExcept(AbstractChange change, InverseValidator validator, Class<? extends Database>... databases) throws Exception {
        List<Class<? extends Database>> databsesToRun = new ArrayList<Class<? extends Database>>();
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            List<Class<? extends Database>> databaseClasses = Arrays.asList(databases);
            if (!databaseClasses.contains(database.getClass())) {
                databsesToRun.add(database.getClass());
            }
        }

        testInverse(change, validator, databsesToRun.toArray(new Class[databsesToRun.size()]));
    }

    protected void testInverseOnAll(AbstractChange change, InverseValidator validator) throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            Change[] inverses = change.createInverses();
            try {
                validator.validate(inverses);
            } catch (AssertionError e) {
                AssertionError error = new AssertionError("InverseValidator failed for " + database.getTypeName() + ": " + e.getMessage());
                error.setStackTrace(e.getStackTrace());

                throw error;
            }
        }
    }

    protected void testInverse(AbstractChange change, InverseValidator validator, Class<? extends Database>... databases) throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            List<Class<? extends Database>> databaseClasses = Arrays.asList(databases);
            if (!databaseClasses.contains(database.getClass())) {
                continue;
            }

            Change[] inverses = change.createInverses();
            try {
                validator.validate(inverses);
            } catch (AssertionError e) {
                AssertionError error = new AssertionError("InverseValidator failed for " + database.getTypeName() + ": " + e.getMessage());
                error.setStackTrace(e.getStackTrace());

                throw error;
            }
        }
    }

    @Test
    public void isSupported() throws Exception {
        Change change = createClassUnderTest();
        if (change == null) {
            return;
        }
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            assertEquals("Unexpected availablity on "+database.getTypeName(), !changeIsUnsupported(database), change.supports(database));
        }
    }

    protected boolean changeIsUnsupported(Database database) {
        return false;
    }

    @Test
    public void validate() throws Exception {
        Change change = createClassUnderTest();
        if (change == null) {
            return;
        }
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (change.supports(database)) {
                ValidationErrors validationErrors = change.validate(database);
                assertTrue("no errors found for "+database.getTypeName(), validationErrors.hasErrors());
            }
        }
    }

    protected static interface GenerateAllValidator {
        public void validate(SqlStatement[] statements, Database database);
    }

    protected static interface InverseValidator {
        public void validate(Change[] statements);
    }

}