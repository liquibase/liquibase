package liquibase.servicelocator

import liquibase.change.Change
import liquibase.changelog.ChangeLogHistoryService
import liquibase.command.LiquibaseCommand
import liquibase.database.Database
import liquibase.datatype.LiquibaseDataType
import liquibase.diff.DiffGenerator
import liquibase.diff.compare.DatabaseObjectComparator
import liquibase.diff.output.changelog.ChangeGenerator
import liquibase.executor.Executor
import liquibase.lockservice.LockService
import liquibase.parser.ChangeLogParser
import liquibase.parser.NamespaceDetails
import liquibase.parser.SnapshotParser
import liquibase.precondition.Precondition
import liquibase.serializer.ChangeLogSerializer
import liquibase.serializer.SnapshotSerializer
import liquibase.snapshot.SnapshotGenerator
import liquibase.sqlgenerator.SqlGenerator
import liquibase.structure.DatabaseObject
import liquibase.util.TestUtil
import spock.lang.Specification
import spock.lang.Unroll

class StandardServiceLocatorTest extends Specification {

    @Unroll("#featureName: #type.name")
    def "all classes are listed in service loader files"() {
        when:
        def subclasses = TestUtil.getClasses(type)
        subclasses = subclasses
                .findAll({
                    return (it.getAnnotation(LiquibaseService) == null ? true : !(it.getAnnotation(LiquibaseService).skip()))
                })
        def subclassText = new TreeSet(subclasses.collect({ it.name })).join("\n")

        def loaderFile = getClass().getResourceAsStream("/META-INF/services/" + type.getName())

        then:
        loaderFile.text.trim().replace("\r", "") == subclassText

        where:
        type << [
                Change.class,
                ChangeLogHistoryService.class,
                Database.class,
                ChangeLogParser.class,
                LiquibaseCommand.class,
                LiquibaseDataType.class,
                DiffGenerator.class,
                DatabaseObjectComparator.class,
                ChangeGenerator.class,
                LockService.class,
                NamespaceDetails.class,
                SnapshotParser.class,
                Precondition.class,
                ChangeLogSerializer.class,
                SnapshotSerializer.class,
                DatabaseObject.class,
                SqlGenerator.class,
                SnapshotGenerator.class,
                Executor.class,
        ]
    }
}
