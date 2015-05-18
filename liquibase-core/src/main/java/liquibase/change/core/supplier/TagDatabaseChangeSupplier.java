package liquibase.change.core.supplier;

import liquibase.change.AbstractChange;
import liquibase.change.Change;
import liquibase.change.core.TagDatabaseChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.diff.DiffResult;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;
import liquibase.statement.core.MarkChangeSetRanStatement;

public class TagDatabaseChangeSupplier extends AbstractChangeSupplier<TagDatabaseChange> {

    public TagDatabaseChangeSupplier() {
        super(TagDatabaseChange.class);
    }

    @Override
    public Change[] prepareDatabase(TagDatabaseChange change) throws Exception {
        return new Change[]{new AbstractChange() {
            @Override
            public String getConfirmationMessage() {
                return "Custom change";
            }

            @Override
            public SqlStatement[] generateStatements(Database database) {
                return new SqlStatement[]{
                        new CreateDatabaseChangeLogTableStatement(),
                        new MarkChangeSetRanStatement(new ChangeSet("1", "test", false, false, "com/example/test.xml", null, null, new DatabaseChangeLog("com/example/test.xml")), ChangeSet.ExecType.EXECUTED),
                        new MarkChangeSetRanStatement(new ChangeSet("2", "test", false, false, "com/example/test.xml", null, null, new DatabaseChangeLog("com/example/test.xml")), ChangeSet.ExecType.EXECUTED),
                        new MarkChangeSetRanStatement(new ChangeSet("3", "test", false, false, "com/example/test.xml", null, null, new DatabaseChangeLog("com/example/test.xml")), ChangeSet.ExecType.EXECUTED),
                        new MarkChangeSetRanStatement(new ChangeSet("4", "test", false, false, "com/example/test.xml", null, null, new DatabaseChangeLog("com/example/test.xml")), ChangeSet.ExecType.EXECUTED)
                };

            }
        }
        };
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, TagDatabaseChange change) throws Exception {
//        Database database = diffResult.getComparisonSnapshot().getDatabase();
//        int rows = ExecutorService.getInstance().getExecutor(database).queryForInt(new RawSqlStatement("select count(*) from " + database.getDatabaseChangeLogTableName() + " where tag='" + change.getTag() + "'"));
//        assertTrue(rows > 0);

    }
}
