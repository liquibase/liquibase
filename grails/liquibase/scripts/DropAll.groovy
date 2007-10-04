import org.codehaus.groovy.grails.compiler.support.*
import java.io.OutputStreamWriter;

Ant.property(environment: "env")
grailsHome = Ant.antProject.properties."env.GRAILS_HOME"
includeTargets << new File("scripts/LiquibaseSetup.groovy")

task ('default':'''Drops all objects in database.
Example: grails dropAll
''') {
    depends(setup)

    try {
        migrator.dropAll();
    }
    catch (Exception e) {
        e.printStackTrace()
        event("StatusFinal", ["Failed to drop database ${grailsEnv}"])
        exit(1)
    } finally {
        migrator.getDatabase().getConnection().close();
    }
}
