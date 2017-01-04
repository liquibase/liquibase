package liquibase.resource

import spock.lang.Specification
import spock.lang.Unroll

import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class ClassLoaderResourceAccessorTest extends Specification {

    def "rootUrls populated"() {
        when:
        def accessor = new ClassLoaderResourceAccessor(new URLClassLoader([
                new File(System.getProperty("java.io.tmpdir")).toURL()].toArray() as URL[]
        ))

        then:
        accessor.getRootPaths().size() >= 3
        accessor.getRootPaths().findAll({ it.endsWith("/test-classes/") }).size() == 1
        accessor.getRootPaths().findAll({ it.endsWith("/classes/") }).size() == 1
    }

    @Unroll("#featureName: #relativeTo / #path -> #expected")
    def "convertToPath using relative paths"() {
        when:
        def accessor = new ClassLoaderResourceAccessor(new URLClassLoader([].toArray() as URL[]))

        then:
        accessor.convertToPath(relativeTo, path) == expected

        where:
        relativeTo                         | path                             | expected
        null                               | "liquibase/Liquibase.class"      | "liquibase/Liquibase.class"
        ""                                 | "liquibase/Liquibase.class"      | "liquibase/Liquibase.class"
        "liquibase"                        | "Liquibase.class"                | "liquibase/Liquibase.class"
        "liquibase"                        | "Contexts.class"                 | "liquibase/Contexts.class"
        "liquibase/Liquibase.class"        | "Contexts.class"                 | "liquibase/Contexts.class"
        "liquibase/"                       | "sql/Sql.class"                  | "liquibase/sql/Sql.class"
        "liquibase"                        | "sql/Sql.class"                  | "liquibase/sql/Sql.class"
        "liquibase/Liquibase.class"        | "sql/Sql.class"                  | "liquibase/sql/Sql.class"
        "liquibase/sql"                    | "../Liquibase.class"             | "liquibase/Liquibase.class"
    }

    def "can recursively enumerate files inside JARs on the classpath"() {
        given:
        def accessor = new ClassLoaderResourceAccessor(Thread.currentThread().contextClassLoader)

        when:
        def listedResources = accessor.list(null, "org/apache/log4j", true, false, true)

        then:
        listedResources.contains("org/apache/log4j/Logger.class") == true
        listedResources.contains("org/apache/log4j/spi/Filter.class") == true
    }

    def "can non-recursively enumerate files inside JARs on the classpath"() {
        given:
        def accessor = new ClassLoaderResourceAccessor(Thread.currentThread().contextClassLoader)

        when:
        def listedResources = accessor.list(null, "org/apache/log4j", true, false, false)

        then:
        listedResources.contains("org/apache/log4j/Logger.class") == true
        listedResources.contains("org/apache/log4j/spi/Filter.class") == false
    }

    def "can list all the files inside JAR file " () {
        given:
        File jarFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "myapp.jar")
        FileOutputStream fout = new FileOutputStream(jarFile);
        JarOutputStream jarOut = new JarOutputStream(fout);
        jarOut.putNextEntry(new ZipEntry("updates/"));
        addFileToJar(jarOut, "updates/first.xml")
        addFileToJar(jarOut, "updates/second.xml")
        addFileToJar(jarOut, "updates/third.xml")
        addFileToJar(jarOut, "updates/fourth.xml")
        jarOut.close();
        fout.close();

        def accessor = new ClassLoaderResourceAccessor(new URLClassLoader([
                new File(System.getProperty("java.io.tmpdir")).toURL()].toArray() as URL[]
        ))

        when:
        String path = "jar:file:" + jarFile.getAbsolutePath() + "!/updates/"

        Set<String> files = accessor.list(null, path, true, false, true)

        then:
        files.size() == 5
        files.contains("updates/")
        files.contains("updates/first.xml")
        files.contains("updates/second.xml")
        files.contains("updates/third.xml")
        files.contains("updates/fourth.xml")
    }

    private void addFileToJar(JarOutputStream jarOut, String filePath) {
        jarOut.putNextEntry(new ZipEntry(filePath));
        jarOut.write("<xml>xml</xml>".bytes);
        jarOut.closeEntry();
    }


}
