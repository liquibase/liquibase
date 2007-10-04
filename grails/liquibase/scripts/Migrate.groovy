import org.codehaus.groovy.grails.compiler.support.*

Ant.property(environment: "env")
grailsHome = Ant.antProject.properties."env.GRAILS_HOME"
includeTargets << new File("scripts/LiquibaseSetup.groovy")

task ('default':"Updates a database to the current version.") {
    depends(setup)

    try {
        System.out.println("Migrating ${grailsEnv} database");
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
