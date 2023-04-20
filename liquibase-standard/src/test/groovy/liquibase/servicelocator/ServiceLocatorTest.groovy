package liquibase.servicelocator;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.parser.ChangeLogParser;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.test.TestContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test
import spock.lang.Specification;

import java.lang.reflect.Modifier;

import static org.junit.Assert.*;

class ServiceLocatorTest extends Specification {

    private CompositeResourceAccessor resourceAccessor;

    def setup() throws Exception{
        resourceAccessor = new CompositeResourceAccessor(new ClassLoaderResourceAccessor(), TestContext.getInstance().getTestResourceAccessor());

    }

    def "findInstance"() throws Exception {
        expect:

        Scope.child(Scope.Attr.resourceAccessor, resourceAccessor, {

            List databases = Scope.getCurrentScope().getServiceLocator().findInstances(Database.class);
            for (def database : databases) {
                def clazz = database.class

                assertFalse(clazz.getName() + " is abstract", Modifier.isAbstract(clazz.getModifiers()));
                assertFalse(clazz.getName() + " is an interface", Modifier.isInterface(clazz.getModifiers()));
                assertNotNull(clazz.getConstructors());
            }
            assertTrue(databases.size() > 0);
        });
    }

//    @Test
//    public void extractZipFile() throws MalformedURLException {
//        File zipFile = ServiceLocator.extractZipFile(new URL(
//                "jar:file:/C:/My%20Projects/liquibase2/liquibase-integration-tests/src/test/resources/ext/jars/liquibase-samplesqlgenerator.jar!/liquibase/sqlgenerator"));
//        assertEquals("C:/My Projects/liquibase2/liquibase-integration-tests/src/test/resources/ext/jars/liquibase-samplesqlgenerator.jar", zipFile.toString().replace(
//                '\\', '/'));
//        zipFile = ServiceLocator.extractZipFile(new URL(
//                "jar:file:/home/myuser/liquibase2/liquibase-integration-tests/src/test/resources/ext/jars/liquibase-samplesqlgenerator.jar!/liquibase/sqlgenerator"));
//        assertEquals("/home/myuser/liquibase2/liquibase-integration-tests/src/test/resources/ext/jars/liquibase-samplesqlgenerator.jar", zipFile.toString().replace('\\',
//                '/'));
//    }
}
