package liquibase.groovyDbTest.pgsql


import liquibase.Scope
import liquibase.command.util.CommandUtil
import liquibase.executor.ExecutorService
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.logging.mdc.customobjects.SimpleStatus
import liquibase.statement.core.RawParameterizedSqlStatement
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class PostgresSQLIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem db = Scope.currentScope.getSingleton(TestSystemFactory).getTestSystem("postgresql") as DatabaseTestSystem

//    def "test status run during update"() throws Exception {
//        given:
//        def changeLogFile = "changelogs/pgsql/pg_sleep.sql"
//        def statusOutputBeforeUpdate = CommandUtil.runStatus(db, changeLogFile)
//
//        when:
//        def statusOutputDuringUpdate
//        def uThread = new UpdateThread(db, changeLogFile)
//        uThread.start()
//        uThread.wait(3000)
//        statusOutputDuringUpdate = CommandUtil.runStatus(db, changeLogFile)
//
//        and:
//        def statusOutputAfterUpdate = CommandUtil.runStatus(db, changeLogFile)
//
//        then:
//        ((SimpleStatus)statusOutputBeforeUpdate.getResult("status")).getMessage() == "undeployed"
//        ((SimpleStatus)statusOutputBeforeUpdate.getResult("status")).getChangesetCount() == 1
//
//        ((SimpleStatus)statusOutputDuringUpdate.getResult("status")).getMessage() == "undeployed"
//        ((SimpleStatus)statusOutputDuringUpdate.getResult("status")).getChangesetCount() == 1
//
//        ((SimpleStatus)statusOutputAfterUpdate.getResult("status")).getMessage() == "up-to-date"
//        ((SimpleStatus)statusOutputAfterUpdate.getResult("status")).getChangesetCount() == 0
//    }

    def "test Blob types changelog"() {
        given:
        def changeLogFile = "changelogs/pgsql/complete/testBlob.changelog.xml"
        CommandUtil.runUpdate(db, changeLogFile)

        when:
        List<Map<String, ?>>  data = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", db.getDatabaseFromFactory())
                .queryForList(
                        new RawParameterizedSqlStatement("SELECT pg_column_size(content_bytea) as BYTEASIZE, pg_column_size(lo_get(content_oid)) as OIDSIZE FROM  public.blobtest"))
        then:
        noExceptionThrown()
        data.get(0) != null
        ((Integer)data.get(0).get("BYTEASIZE")) > 0
        data.get(0).get("BYTEASIZE") == data.get(0).get("OIDSIZE")
    }
}
