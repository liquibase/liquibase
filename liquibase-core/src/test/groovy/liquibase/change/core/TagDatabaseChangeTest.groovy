package liquibase.change.core

import liquibase.change.ChangeStatus;
import liquibase.change.StandardChangeTest
import liquibase.changelog.ChangeLogHistoryService
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.database.core.MockDatabase
import spock.lang.Unroll

public class TagDatabaseChangeTest extends StandardChangeTest {

    def cleanup() {
        ChangeLogHistoryServiceFactory.reset()
    }

    def getConfirmationMessage() throws Exception {
        when:
        def change = new TagDatabaseChange()
        change.setTag("TAG_NAME");

        then:
        "Tag 'TAG_NAME' applied to database" == change.getConfirmationMessage()
    }

    @Unroll
    def "checkStatus"() {
        when:
        def change = new TagDatabaseChange()
        change.setTag("test_tag")

        def database = new MockDatabase()
        ChangeLogHistoryService historyService = Mock()
        ChangeLogHistoryServiceFactory historyServiceFactory = Mock()

        ChangeLogHistoryServiceFactory.instance = historyServiceFactory
        historyServiceFactory.getChangeLogService(database) >> historyService

        historyService.tagExists(change.tag) >> tagExists

        then:
        assert change.checkStatus(database).status == expectedStatus

        where:
        tagExists | expectedStatus
        true  | ChangeStatus.Status.complete
        false | ChangeStatus.Status.notApplied


    }
}