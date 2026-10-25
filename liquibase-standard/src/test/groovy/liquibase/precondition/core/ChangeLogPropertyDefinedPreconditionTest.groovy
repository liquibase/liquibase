package liquibase.precondition.core

import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.changelog.visitor.ValidatingVisitor
import liquibase.database.core.MockDatabase
import liquibase.exception.PreconditionFailedException
import liquibase.parser.core.yaml.YamlChangeLogParser
import liquibase.test.JUnitResourceAccessor
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
        childChangelog.setChangeLogParameters(changeLogParameters)

        then:
        precondition.setProperty(property)
        precondition.setValue(value)
        if (null == expectedEx) {
            precondition.check(database, childChangelog, changeSet, null)
        } else {
            def exceptionThrown = false
            try {
                precondition.check(database, childChangelog, changeSet, null)
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

    def 'ChangeLogPropertyDefinedPrecondition property required' () {
        def precondition = new ChangeLogPropertyDefinedPrecondition()
        when:
        List<String> v = precondition.validate(new MockDatabase()).requiredErrorMessages
        then:
        v.size() == 1
        v.any {it.contains('property') && it.contains('is required')}
        when:
        precondition.property = ' '
        v = precondition.validate(new MockDatabase()).errorMessages
        then:
        v.size() == 1
        v.any {it.contains('property') && it.contains('is empty')}
    }

    def "included changeset can access #c property from parent" () {
        ValidatingVisitor vv = new ValidatingVisitor()
        DatabaseChangeLog rootChangeLog = new YamlChangeLogParser().parse("properties/${c}.yml",
                                          new ChangeLogParameters(), new JUnitResourceAccessor());
        vv.validate(new MockDatabase(), rootChangeLog)
        expect:
        vv.validationPassed()
        where:
        c << ['local', 'global']
    }
}
