package liquibase.servicelocator

import liquibase.change.Change
import liquibase.changelog.ChangeLogHistoryService
import liquibase.command.LiquibaseCommand
import liquibase.database.Database
import liquibase.datatype.LiquibaseDataType
import liquibase.diff.DiffGenerator
import liquibase.diff.output.changelog.ChangeGenerator
import liquibase.executor.Executor
import liquibase.lockservice.LockService
import liquibase.parser.ChangeLogParser
import liquibase.diff.compare.DatabaseObjectComparator
import liquibase.parser.NamespaceDetails
import liquibase.parser.SnapshotParser
import liquibase.precondition.Precondition
import liquibase.serializer.ChangeLogSerializer
import liquibase.serializer.SnapshotSerializer
import liquibase.snapshot.SnapshotGenerator
import liquibase.sqlgenerator.SqlGenerator
import liquibase.structure.DatabaseObject
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.reflect.Modifier

class StandardServiceLocatorTest extends Specification {

    @Unroll("#featureName: #type.name")
    def "all classes are listed in service loader files"() {
        def basedir = new File(getClass().getResource("/" + type.name.replace(".", "/") + ".class").toURI()).parentFile

        def first = type.name.replaceFirst(/\.\w+$/, "").replace(".", File.separatorChar as String)
        def rootDir = basedir.toString().replaceFirst(first.replace("\\", "\\\\")+"\$", "")

        def subclasses = new TreeSet<String>()

        expect:
        basedir.eachFileRecurse { file ->
            if (file.name.endsWith(".class") && !file.name.contains("\$")) {
                def potentialClass = Class.forName(file.absolutePath.substring(rootDir.length()).replace("\\", ".")
                        .replace("/", ".").replaceFirst(/\.class$/, ""))
                if (type.isAssignableFrom(potentialClass) && !type.equals(potentialClass) && !Modifier.isAbstract(potentialClass.getModifiers())) {
                    try {
                        potentialClass.getConstructor()
                        subclasses.add(potentialClass.name)
                    } catch (NoSuchMethodException ignored) {
                        //don't add it to the list
                    }
                }
            }
        }

        def loaderFile = getClass().getResourceAsStream("/META-INF/services/" + type.getName())
        loaderFile.text.trim().replace("\r", "") == subclasses.join("\n")

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
