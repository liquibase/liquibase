package liquibase.testsuites

import liquibase.action.core.CreateTableActionTest
import liquibase.action.core.DropTableActionTest
import liquibase.action.core.SnapshotColumnsActionTest
import liquibase.action.core.SnapshotDatabaseObjectsActionTablesTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite.class)
@Suite.SuiteClasses([SnapshotDatabaseObjectsActionTablesTest, SnapshotColumnsActionTest, CreateTableActionTest, DropTableActionTest])
class ActionTests {

}
