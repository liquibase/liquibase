package liquibase.change.core

import liquibase.Scope
import liquibase.change.ChangeStatus;
import liquibase.change.StandardChangeTest
import liquibase.changelog.ChangeLogHistoryService
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.database.core.MockDatabase
import liquibase.plugin.Plugin
import spock.lang.Unroll

public class TagDatabaseChangeTest extends StandardChangeTest {

    ChangeLogHistoryServiceFactory changeLogHistoryServiceFactory = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class)


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
        historyService.tagExists(change.tag) >> tagExists
        historyService.getPriority() >> Plugin.PRIORITY_SPECIALIZED
        historyService.supports(database) >> true
        changeLogHistoryServiceFactory.register(historyService)

        then:
        assert change.checkStatus(database).status == expectedStatus
        changeLogHistoryServiceFactory.unregister(historyService)

        where:
        tagExists | expectedStatus
        true  | ChangeStatus.Status.complete
        false | ChangeStatus.Status.notApplied


    }
}
