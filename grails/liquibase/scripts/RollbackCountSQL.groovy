import org.codehaus.groovy.grails.compiler.support.*
import java.io.OutputStreamWriter;

Ant.property(environment: "env")
grailsHome = Ant.antProject.properties."env.GRAILS_HOME"
includeTargets << new File("scripts/LiquibaseSetup.groovy")

task ('default':'''Writes SQL to roll back the specified number of changes to STDOUT.
Example: grails rollbackCount 3
''') {
    depends(setup)

    try {
        migrator.setMode(Migrator.Mode.OUTPUT_ROLLBACK_SQL_MODE);
        migrator.setOutputSQLWriter(new OutputStreamWriter(System.out));
        migrator.setRollbackCount(Integer.parseInt(args));
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
