package liquibase.migrator;

import junit.framework.TestCase;

public class ChangeSetTest extends TestCase {

    public void testNothing() {

    }
//    public void testIsChangeSetRan() throws Exception {
//        Connection conn = createMock(Connection.class);
//
//        OracleDatabase database = new OracleDatabase();
//        database.setConnection(conn);
//
//        DatabaseChangeLog changeLog = new DatabaseChangeLog();
//        changeLog.setDatabase(database);
//        changeLog.setInputFilePath("path/to/migration.xml");
//
//        ChangeSet changeSet = new ChangeSet();
//        changeSet.setId("1as");
//        changeSet.setAuthor("testAuthor");
//        changeSet.setDatabaseChangeLog(changeLog);
//        changeSet.setMd5sum("asdf5sum");
//
//        PreparedStatement pstmt = createMock(PreparedStatement.class);
//        expect(conn.prepareStatement("select md5sum from DatabaseChangeLog where id=? AND author=? AND filename=?")).andReturn(pstmt).atLeastOnce();
//
//        pstmt.setString(1, "1as");
//        expectLastCall().atLeastOnce();
//        pstmt.setString(2, "testAuthor");
//        expectLastCall().atLeastOnce();
//        pstmt.setString(3, "path/to/migration.xml");
//        expectLastCall().atLeastOnce();
//
//        ResultSet rs = createMock(ResultSet.class);
//        expect(pstmt.executeQuery()).andReturn(rs).atLeastOnce();
//
//        expect(rs.next()).andReturn(Boolean.TRUE);
//        expect(rs.getString("md5sum")).andReturn("asdf5sum");
//
//        replay(rs);
//        replay(pstmt);
//        replay(conn);
//
//        assertTrue(changeSet.isChangeSetRan());
//        verify(rs);
//        verify(pstmt);
//        verify(conn);
//    }

//    public void testMarkChangeSetAsRan() throws Exception {
//        Connection conn = createMock(Connection.class);
//
//        OracleDatabase database = new OracleDatabase();
//        database.setConnection(conn);
//
//        DatabaseChangeLog changeLog = new DatabaseChangeLog();
//        changeLog.setDatabase(database);
//        changeLog.setInputFilePath("path/to/migration.xml");
//
//        ChangeSet changeSet = new ChangeSet();
//        changeSet.setId("1as");
//        changeSet.setAuthor("testAuthor");
//        changeSet.setDatabaseChangeLog(changeLog);
//        changeSet.setMd5sum("asdf5sum");
//
//        Statement statement = createMock(Statement.class);
//        expect(conn.createStatement()).andReturn(statement).atLeastOnce();
//
//        expect(statement.executeUpdate("INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM) VALUES ('1as', 'testAuthor', 'path/to/migration.xml', sysdate, 'asdf5sum')")).andStubReturn(1);
//
//        conn.commit();
//        expectLastCall();
//        statement.close();
//        expectLastCall();
//
//        replay(statement);
//        replay(conn);
//
//        changeSet.markChangeSetAsRan(true);
//
//        //test with outputSQL set
//        StringWriter sqlOutputWriter = new StringWriter();
//        changeLog.setSqlOutputWriter(sqlOutputWriter);
//        changeSet.markChangeSetAsRan(true);
//        assertEquals("INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM) VALUES ('1as', 'testAuthor', 'path/to/migration.xml', sysdate, 'asdf5sum');", sqlOutputWriter.getBuffer().toString().trim());
//    }

//    public void testExecute() throws Exception {
//        Connection conn = createMock(Connection.class);
//
//        OracleDatabase database = new OracleDatabase() {
//            public void checkDatabaseChangeLogTable() throws SQLException {
//                return;
//            }
//        };
//        database.setConnection(conn);
//
//        DatabaseChangeLog changeLog = new DatabaseChangeLog();
//        changeLog.setDatabase(database);
//
//        ChangeSet changeSet = new ChangeSet() {
//            public boolean isChangeSetRan() throws DatabaseHistoryException, SQLException {
//                return false;
//            }
//
//            public void markChangeSetAsRan() throws SQLException {
//                return;
//            }
//        };
//        changeSet.setDatabaseChangeLog(changeLog);
//
//        AbstractChange refactoring1 = createMock(AbstractChange.class);
//        refactoring1.executeStatement(database);
//        expectLastCall();
//        expect(refactoring1.getConfirmationMessage()).andReturn("confirmation 1");
//        expect(refactoring1.getRefactoringName()).andStubReturn("asdf");
//        replay(refactoring1);
//
//        AbstractChange refactoring2 = createMock(AbstractChange.class);
//        refactoring2.executeStatement(database);
//        expectLastCall();
//        expect(refactoring2.getConfirmationMessage()).andReturn("confirmation 2");
//        expect(refactoring2.getRefactoringName()).andStubReturn("asdf");
//        replay(refactoring2);
//
//        changeSet.addRefactoring(refactoring1);
//        changeSet.addRefactoring(refactoring2);
//
//        conn.commit();
//        expectLastCall();
//        replay(conn);
//
//        changeSet.execute();
//        verify(conn);
//        verify(refactoring1);
//        verify(refactoring2);
//
//        //------------------------------ test when changeSet already ran
//        changeSet = new ChangeSet() {
//            public boolean isChangeSetRan() throws DatabaseHistoryException, SQLException {
//                return true;
//            }
//        };
//        changeSet.setDatabaseChangeLog(changeLog);
//        changeSet.addRefactoring(refactoring1);
//        changeSet.addRefactoring(refactoring2);
//
//        changeSet.execute();
//
//        //-------------------------- Test exception thrown by statement
//        changeSet = new ChangeSet() {
//            public boolean isChangeSetRan() throws DatabaseHistoryException, SQLException {
//                return false;
//            }
//        };
//        changeSet.setDatabaseChangeLog(changeLog);
//        changeSet.addRefactoring(refactoring1);
//        changeSet.addRefactoring(refactoring2);
//
//        reset(refactoring1);
//        refactoring1.executeStatement(database);
//        expectLastCall().andThrow(new SQLException());
//        replay(refactoring1);
//
//        reset(conn);
//        conn.rollback();
//        expectLastCall();
//        replay(conn);
//
//        try {
//            changeSet.execute();
//            fail("Exception not thrown");
//        } catch (MigrationFailedException e) {
//            ; //that's what we wanted
//        }
//        verify(conn);
//
//    }

//    public void testSave() throws Exception {
//        Connection conn = createMock(Connection.class);
//
//        OracleDatabase database = new OracleDatabase() {
//            public void checkDatabaseChangeLogTable() throws SQLException {
//                return;
//            }
//        };
//
//        DatabaseChangeLog changeLog = new DatabaseChangeLog();
//        changeLog.setDatabase(database);
//
//        ChangeSet changeSet = new ChangeSet() {
//            public boolean isChangeSetRan() throws DatabaseHistoryException, SQLException {
//                return false;
//            }
//
//            public void markChangeSetAsRan() throws SQLException {
//                return;
//            }
//        };
//        changeLog.setInputFilePath("net/sundog/test/db.changelog.xml");
//        changeSet.setDatabaseChangeLog(changeLog);
//        changeSet.setId("1512-12");
//        changeSet.setAuthor("testAuthor");
//
//        Writer writer = createMock(Writer.class);
//        changeLog.setSqlOutputWriter(writer);
//        reset(conn);
//        replay(conn);
//
//        AbstractChange refactoring1 = createMock(AbstractChange.class);
//        AbstractChange refactoring2 = createMock(AbstractChange.class);
//
//        changeSet.addRefactoring(refactoring1);
//        changeSet.addRefactoring(refactoring2);
//
//        refactoring1.saveStatement(database, writer);
//        expectLastCall();
//
//        refactoring2.saveStatement(database, writer);
//        expectLastCall();
//        replay(refactoring1);
//        replay(refactoring2);
//
//        writer.write("-- Changeset "+changeSet.toString()+"\n");
//        expectLastCall();
//        replay(writer);
//
//        changeSet.save(true);
//        verify(conn);
//        verify(refactoring1);
//        verify(refactoring2);
//        verify(writer);
//    }
}