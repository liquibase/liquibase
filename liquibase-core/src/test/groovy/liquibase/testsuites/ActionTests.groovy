package liquibase.testsuites

import liquibase.action.core.CreateTableActionTest
import liquibase.action.core.DropTableActionTest
import liquibase.action.core.SnapshotColumnsActionTest
import liquibase.action.core.SnapshotTablesActionTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite.class)
@Suite.SuiteClasses([SnapshotTablesActionTest, SnapshotColumnsActionTest, CreateTableActionTest, DropTableActionTest])
class ActionTests {

}
