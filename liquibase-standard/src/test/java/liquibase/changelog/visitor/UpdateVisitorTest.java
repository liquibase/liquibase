package liquibase.changelog.visitor;

public class UpdateVisitorTest {

//    @Test
//    public void visit_unrun() throws Exception {
//        Database database = createMock(Database.class);
//        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
//
//        ChangeExecListener listener = createMock(ChangeExecListener.class);
//
//        ChangeSet changeSet = createMock(ChangeSet.class);
//        DatabaseChangeLog databaseChangeLog = new DatabaseChangeLog("test.xml");
//        expect(changeSet.execute(databaseChangeLog, listener, database)).andReturn(ChangeSet.ExecType.EXECUTED);
//
//        expect(database.getRunStatus(changeSet)).andReturn(ChangeSet.RunStatus.NOT_RAN);
//
//        expect(database.getObjectQuotingStrategy()).andReturn(ObjectQuotingStrategy.LEGACY);
//
//        listener.willRun(changeSet, databaseChangeLog, database, RunStatus.NOT_RAN);
//        expectLastCall();
//        listener.ran(changeSet, databaseChangeLog, database, ChangeSet.ExecType.EXECUTED);
//        expectLastCall();
//
//        database.markChangeSetExecStatus(changeSet, ChangeSet.ExecType.EXECUTED);
//        expectLastCall();
//
//        database.commit();
//        expectLastCall();
//
//
//        replay(changeSet);
//        replay(database);
//        replay(listener);
//
//        UpdateVisitor visitor = new UpdateVisitor(database, listener);
//        visitor.visit(changeSet, databaseChangeLog, database, null);
//
//        verify(database);
//        verify(changeSet);
//        verify(listener);
//    }

}
