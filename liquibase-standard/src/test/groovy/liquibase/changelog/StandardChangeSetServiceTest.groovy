package liquibase.changelog


import liquibase.Scope
import liquibase.changelog.DatabaseChangeLog
import liquibase.changeset.ChangeSetServiceFactory
import liquibase.changeset.StandardChangeSetService
import spock.lang.Specification

class StandardChangeSetServiceTest extends Specification {
    def "Test StandardChangeSetService creation"() {
        when:
        def service
        def changeSet
        Map<String, Object> scopeValues = new HashMap<>()
        Scope.child(scopeValues, new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                service = ChangeSetServiceFactory.getInstance().createChangeSetService()
                changeSet =
                   service.createChangeSet("id", "author", true, true,
                           "file.sql", "", null, new DatabaseChangeLog())
            }
        })

        then:
        service != null
        service.getClass() == StandardChangeSetService.class
        changeSet != null
        changeSet.getClass() == ChangeSet.class
    }

}
