import org.codehaus.groovy.grails.compiler.support.*
import liquibase.migrator.Migrator;
import java.io.OutputStreamWriter;
import java.util.*;
import java.text.*;

Ant.property(environment: "env")
grailsHome = Ant.antProject.properties."env.GRAILS_HOME"
includeTargets << new File("scripts/LiquibaseSetup.groovy")

task ('default':'''Rolls back the specified date.
Example: grails rollbackToDate 2007-05-15 18:15:12 
''') {
    depends(setup)

    try {
        migrator.setMode(Migrator.Mode.EXECUTE_ROLLBACK_MODE);
        if (commandParams == null) {
            throw new RuntimeException("rollbackToDate requires a rollback date");
        }
        def DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        migrator.setRollbackToDate(dateFormat.parse(args));
        migrator.migrate()
//            if (migrate.migrate()) {
//                System.out.println("Database migrated");
//            } else {
//                System.out.println("Database up-to-date");
//            }
    }
    catch (Exception e) {
        e.printStackTrace()
        event("StatusFinal", ["Failed to migrate database ${grailsEnv}"])
        exit(1)
    } finally {
        migrator.getDatabase().getConnection().close();
    }                                                         
}
