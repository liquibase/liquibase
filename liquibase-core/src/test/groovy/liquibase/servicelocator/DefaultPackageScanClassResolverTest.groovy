package liquibase.servicelocator

import liquibase.database.Database
import liquibase.database.core.MySQLDatabase
import liquibase.database.core.OracleDatabase
import liquibase.logging.Logger
import liquibase.logging.core.AbstractLogger
import liquibase.logging.core.Slf4jLogger
import spock.lang.Specification

class DefaultPackageScanClassResolverTest extends Specification {

    def "can find classes"() {
        when:
        def resolver = new DefaultPackageScanClassResolver()
        resolver.addClassLoader(this.getClass().getClassLoader())

        def loggingClasses = new HashSet<Class>()
        resolver.find(new AssignableToPackageScanFilter(Logger.class), "liquibase.logging", loggingClasses)

        then:
        loggingClasses.size() > 0
        assert loggingClasses.contains(Slf4jLogger.class)
        assert !loggingClasses.contains(AbstractLogger.class)

        when:
        def databaseClasses = new HashSet<Class>()
        resolver.find(new AssignableToPackageScanFilter(Database.class), "liquibase.database", databaseClasses)

        then:
        databaseClasses.size() > 0
        databaseClasses.contains(OracleDatabase.class)
        databaseClasses.contains(MySQLDatabase.class)
    }
}
