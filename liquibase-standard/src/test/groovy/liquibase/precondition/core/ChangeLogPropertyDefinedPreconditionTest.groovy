package liquibase.precondition.core

import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.database.core.MockDatabase
import liquibase.exception.PreconditionFailedException
import spock.lang.Specification

class ChangeLogPropertyDefinedPreconditionTest extends Specification {

    def "evaluate global and local properties correctly"() {
        when:
        def changeLog = new DatabaseChangeLog("com/example/changelog.txt")
        def childChangelog = new DatabaseChangeLog("com/example/child.txt")
        def changeSet = new ChangeSet(childChangelog)
        def database = new MockDatabase()
        def changeLogParameters = new ChangeLogParameters(database)
        def precondition = new ChangeLogPropertyDefinedPrecondition()

        changeSet.setFilePath(childChangelog.getFilePath())

        changeLogParameters.set("globalKey", "globalValue")
        changeLogParameters.setLocal("localKey", "localValue", childChangelog)
        changeLog.setChangeLogParameters(changeLogParameters)

        then:
        precondition.setProperty(property)
        precondition.setValue(value)
        if (null == expectedEx) {
            precondition.check(database, changeLog, changeSet, null)
        } else {
            def exceptionThrown = false
            try {
                precondition.check(database, changeLog, changeSet, null)
            } catch (Throwable th) {
                expectedEx === th.getClass()
                exceptionThrown = true
            }
            exceptionThrown == true
        }

        where:
        property            | value             | expectedEx
        "globalKey"         | "globalValue"     | null
        "localKey"          | "localValue"      | null
        "nonExistentKey"    | null              | PreconditionFailedException.class
    }
}
