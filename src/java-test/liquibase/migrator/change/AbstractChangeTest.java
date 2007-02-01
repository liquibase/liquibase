package liquibase.migrator.change;

import junit.framework.TestCase;
import liquibase.database.AbstractDatabase;
import liquibase.database.OracleDatabase;
import liquibase.database.struture.*;
import org.easymock.EasyMock;
import static org.easymock.classextension.EasyMock.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.sql.*;
import java.util.Set;

public abstract class AbstractChangeTest extends TestCase {

    public abstract void testGetRefactoringName() throws Exception;

    public abstract void testGenerateStatement() throws Exception;

    public abstract void testGetConfirmationMessage() throws Exception;

    public abstract void testIsApplicableTo() throws Exception;

    protected Column createColumnDatabaseStructure() throws SQLException {
        return new Column(null, null, -1, null, -1, -1, -1, null, null);
    }

    protected Table createTableDatabaseStructure() throws SQLException {
        ResultSet rs = EasyMock.createMock(ResultSet.class);
        EasyMock.expect(rs.getString("TABLE_NAME")).andStubReturn(null);
        EasyMock.expect(rs.getString("TABLE_CAT")).andStubReturn(null);
        EasyMock.expect(rs.getString("TABLE_SCHEM")).andStubReturn(null);
        EasyMock.expect(rs.getString("TABLE_TYPE")).andStubReturn(null);
        EasyMock.expect(rs.getString("REMARKS")).andStubReturn(null);
        EasyMock.replay(rs);

        return new Table(null, null, null, null, null, null);
    }

    protected Sequence createSequenceDatabaseStructure() throws SQLException {
        return new Sequence("SEQ_NAME", null, null, null);
    }

    protected Index createIndexDatabaseStructure() throws SQLException {
        return new Index();
    }

    protected DatabaseSystem createDatabaseSystem() throws SQLException {
        Connection conn = createMock(Connection.class);
        DatabaseMetaData metaData = createMock(DatabaseMetaData.class);

        expect(conn.getMetaData()).andReturn(metaData);
        expect(metaData.getURL()).andReturn("jdbc:test:url");
        replay(conn);
        replay(metaData);

        return new DatabaseSystem(conn);
    }

    public abstract void testCreateNode() throws Exception;

    public void testSaveStatement() throws Exception {
        AbstractChange change = new AbstractChange("test", "Test Refactoring") {
            public String generateStatement(AbstractDatabase database) {
                return "GENERATED STATEMENT";
            }

            public String getConfirmationMessage() {
                return null;
            }

            public boolean isApplicableTo(Set<DatabaseStructure> selectedDatabaseStructures) {
                return false;
            }

            public Element createNode(Document currentMigrationFileDOM) {
                return null;
            }
        };

        StringWriter stringWriter = new StringWriter();

        OracleDatabase database = new OracleDatabase();
        change.saveStatement(database, stringWriter);

        assertEquals("GENERATED STATEMENT;\n\n", stringWriter.getBuffer().toString());
    }

    public void testExecuteStatement() throws Exception {
        AbstractChange change = new AbstractChange("test", "Test Refactorign") {
            public String generateStatement(AbstractDatabase database) {
                return "GENERATED STATEMENT;";
            }

            public String getConfirmationMessage() {return null; }

            public boolean isApplicableTo(Set<DatabaseStructure> selectedDatabaseStructures) { return false; }

            public Element createNode(Document currentMigrationFileDOM) { return null; }
        };

        Connection conn = createMock(Connection.class);
        Statement statement = createMock(Statement.class);
        expect(conn.createStatement()).andReturn(statement);
        expect(statement.execute("GENERATED STATEMENT;")).andStubReturn(true);
        statement.close();
        expectLastCall();
        replay(conn);
        replay(statement);

        OracleDatabase database = new OracleDatabase();
        database.setConnection(conn);

        change.executeStatement(database);
    }
}