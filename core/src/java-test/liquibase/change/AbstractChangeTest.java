package liquibase.change;

import liquibase.database.Database;
import liquibase.database.OracleDatabase;
import liquibase.database.statement.RawSqlStatement;
import liquibase.database.statement.SqlStatement;
import liquibase.database.statement.visitor.SqlVisitor;
import liquibase.database.structure.DatabaseObject;
import liquibase.util.StreamUtil;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.test.TestContext;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.junit.Assert.*;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

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
    public void saveStatement() throws Exception {
        Change change = new AbstractChange("test", "Test Refactoring") {
            public SqlStatement[] generateStatements(Database database) {
                return new SqlStatement[]{new RawSqlStatement("GENERATED STATEMENT")};
            }

            public String getConfirmationMessage() {
                return null;
            }

            public void validate(Database database) throws InvalidChangeDefinitionException {

            }
        };

        StringWriter stringWriter = new StringWriter();

        OracleDatabase database = new OracleDatabase();
        database.saveStatements(change, new ArrayList<SqlVisitor>(), stringWriter);

        assertEquals("GENERATED STATEMENT;" + StreamUtil.getLineSeparator() + StreamUtil.getLineSeparator(), stringWriter.getBuffer().toString());
    }

    @Test
    public void executeStatement() throws Exception {
        Change change = new AbstractChange("test", "Test Refactorign") {
            public SqlStatement[] generateStatements(Database database) {
                return new SqlStatement[]{new RawSqlStatement("GENERATED STATEMENT;")};
            }

            public String getConfirmationMessage() {
                return null;
            }

            public void validate(Database database) throws InvalidChangeDefinitionException {

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
                AssertionError error = new AssertionError("GenerateAllValidator failed for " + database.getProductName() + ": " + e.getMessage());
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
                AssertionError error = new AssertionError("GenerateAllValidator failed for " + database.getProductName() + ": " + e.getMessage());
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
                AssertionError error = new AssertionError("InverseValidator failed for " + database.getProductName() + ": " + e.getMessage());
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
                AssertionError error = new AssertionError("InverseValidator failed for " + database.getProductName() + ": " + e.getMessage());
                error.setStackTrace(e.getStackTrace());

                throw error;
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