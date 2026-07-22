package liquibase.servicelocator

import liquibase.Scope
import liquibase.change.Change
import liquibase.changelog.ChangeLogHistoryService
import liquibase.database.Database
import liquibase.database.DatabaseConnection
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
import org.junit.Assume
import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Unroll

import javax.tools.ToolProvider
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class StandardServiceLocatorTest extends Specification {

    @TempDir
    Path tempDir

    @Unroll("#featureName: #type.name")
    def "all classes are listed in service loader files"() {
        when:
        Assume.assumeTrue(this.class.name == "liquibase.servicelocator.StandardServiceLocatorTest")
        def subclasses = TestUtil.getClasses(type)
        subclasses = subclasses
                .findAll({
                    return (it.getAnnotation(LiquibaseService) == null ? true : !(it.getAnnotation(LiquibaseService).skip()))
                })
        def subclassText = new TreeSet(subclasses.collect({ it.name })).join("\n")

        Assume.assumeFalse("No "+type.name+" classes found", subclasses.size() == 0)

        def loaderFile = new File("src/main/resources/META-INF/services/" + type.getName())

        then:
        loaderFile.text.trim().replace("\r", "") == subclassText

        where:
        type << [
                Change.class,
                ChangeLogHistoryService.class,
                Database.class,
                ChangeLogParser.class,
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
                DatabaseConnection.class,
        ]
    }

    def "findInstances continues after NoClassDefFoundError from ServiceLoader.hasNext"() {
        given:
        def compiler = ToolProvider.getSystemJavaCompiler()
        Assume.assumeNotNull("JDK JavaCompiler required to build broken provider fixtures", compiler)

        def classesDir = tempDir.resolve("classes")
        Files.createDirectories(classesDir)
        compileServiceFixtures(classesDir)
        Files.delete(classesDir.resolve("servicelocator/testfixture/MissingDependency.class"))

        URLClassLoader serviceClassLoader = new URLClassLoader(
                [classesDir.toUri().toURL()] as URL[],
                getClass().getClassLoader()
        )
        def serviceType = serviceClassLoader.loadClass("servicelocator.testfixture.OptionalDependencyService")

        when:
        def instances = Scope.child([(Scope.Attr.classLoader.name()): serviceClassLoader], {
            return new StandardServiceLocator().findInstances(serviceType)
        } as Scope.ScopedRunnerWithReturn)

        then:
        instances.size() == 2
        instances*.getClass().name as Set == [
                "servicelocator.testfixture.GoodProviderBefore",
                "servicelocator.testfixture.GoodProviderAfter",
        ] as Set

        cleanup:
        serviceClassLoader?.close()
    }

    def "findInstances returns empty list when every provider fails to load"() {
        given:
        def compiler = ToolProvider.getSystemJavaCompiler()
        Assume.assumeNotNull("JDK JavaCompiler required to build broken provider fixtures", compiler)

        def classesDir = tempDir.resolve("classes-all-bad")
        Files.createDirectories(classesDir)
        compileServiceFixtures(classesDir)
        Files.delete(classesDir.resolve("servicelocator/testfixture/MissingDependency.class"))

        def servicesFile = classesDir.resolve("META-INF/services/servicelocator.testfixture.OptionalDependencyService")
        Files.write(servicesFile, "servicelocator.testfixture.BadProvider\n".getBytes(StandardCharsets.UTF_8))

        URLClassLoader serviceClassLoader = new URLClassLoader(
                [classesDir.toUri().toURL()] as URL[],
                getClass().getClassLoader()
        )
        def serviceType = serviceClassLoader.loadClass("servicelocator.testfixture.OptionalDependencyService")

        when:
        def instances = Scope.child([(Scope.Attr.classLoader.name()): serviceClassLoader], {
            return new StandardServiceLocator().findInstances(serviceType)
        } as Scope.ScopedRunnerWithReturn)

        then:
        instances.isEmpty()

        cleanup:
        serviceClassLoader?.close()
    }

    private void compileServiceFixtures(Path classesDir) {
        def srcDir = tempDir.resolve("src-" + classesDir.fileName)
        def pkgDir = srcDir.resolve("servicelocator/testfixture")
        Files.createDirectories(pkgDir)

        writeJava(pkgDir.resolve("OptionalDependencyService.java"), """
            package servicelocator.testfixture;
            public interface OptionalDependencyService {
            }
            """)
        writeJava(pkgDir.resolve("MissingDependency.java"), """
            package servicelocator.testfixture;
            public class MissingDependency {
            }
            """)
        writeJava(pkgDir.resolve("GoodProviderBefore.java"), """
            package servicelocator.testfixture;
            public class GoodProviderBefore implements OptionalDependencyService {
                public GoodProviderBefore() {}
            }
            """)
        writeJava(pkgDir.resolve("BadProvider.java"), """
            package servicelocator.testfixture;
            public class BadProvider extends MissingDependency implements OptionalDependencyService {
                public BadProvider() {}
            }
            """)
        writeJava(pkgDir.resolve("GoodProviderAfter.java"), """
            package servicelocator.testfixture;
            public class GoodProviderAfter implements OptionalDependencyService {
                public GoodProviderAfter() {}
            }
            """)

        def sources = Files.walk(srcDir)
                .filter { Files.isRegularFile(it) && it.toString().endsWith(".java") }
                .map { it.toFile() }
                .toList()

        def compiler = ToolProvider.getSystemJavaCompiler()
        def fileManager = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8)
        def compilationUnits = fileManager.getJavaFileObjectsFromFiles(sources)
        def options = ["-d", classesDir.toAbsolutePath().toString()]
        def task = compiler.getTask(null, fileManager, null, options, null, compilationUnits)
        assert task.call() : "Failed to compile service locator test fixtures"
        fileManager.close()

        def servicesDir = classesDir.resolve("META-INF/services")
        Files.createDirectories(servicesDir)
        Files.write(servicesDir.resolve("servicelocator.testfixture.OptionalDependencyService"), """
            servicelocator.testfixture.GoodProviderBefore
            servicelocator.testfixture.BadProvider
            servicelocator.testfixture.GoodProviderAfter
            """.stripIndent().trim().getBytes(StandardCharsets.UTF_8))
    }

    private static void writeJava(Path path, String source) {
        Files.write(path, source.stripIndent().trim().getBytes(StandardCharsets.UTF_8))
    }
}
