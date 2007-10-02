import org.codehaus.groovy.grails.compiler.support.*
import liquibase.migrator.Migrator;
import liquibase.CompositeFileOpener;
import liquibase.FileOpener;
import liquibase.FileSystemFileOpener;
import org.liquibase.grails.GrailsFileOpener;

Ant.property(environment: "env")
grailsHome = Ant.antProject.properties."env.GRAILS_HOME"

includeTargets << new File("${grailsHome}/scripts/Compile.groovy")

config = new ConfigObject()

migrator = null

task ('setup' : "Migrates the current database to the latest") {
    profile("compiling config") {
        compile()
    }

    profile("creating config object") {
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader()
        classLoader = new URLClassLoader([classesDir.toURL()] as URL[], contextLoader)
        def configSlurper = new ConfigSlurper(grailsEnv)
        def configFile = new File("${basedir}/grails-app/conf/Config.groovy")
        if (configFile.exists()) {
            try {

                config = configSlurper.parse(classLoader.loadClass("Config"))
                config.setConfigFile(configFile.toURL())

//                ConfigurationHolder.setConfig(config)
            }
            catch (Exception e) {
                e.printStackTrace()

                event("StatusFinal", ["Failed to compile configuration file ${configFile}: ${e.message}"])
                exit(1)
            }

        }
        def dataSourceFile = new File("${basedir}/grails-app/conf/DataSource.groovy")
        if (dataSourceFile.exists()) {
            try {
                def dataSourceConfig = configSlurper.parse(classLoader.loadClass("DataSource"))
                config.merge(dataSourceConfig)
//                ConfigurationHolder.setConfig(config)
            }
            catch (Exception e) {
                e.printStackTrace()

                event("StatusFinal", ["Failed to compile data source file $dataSourceFile: ${e.message}"])
                exit(1)
            }
        }
        classLoader = contextLoader;
    }

    profile("automigrate the current database") {
        Properties p = config.dataSource.toProperties();
        p.driver = p.driverClassName;
        p.user = p.username;
        if (p.password == null) {
            p.password = ""
        }
        p.packageName = "grails-app.migrations"
        p.auto = "true";

        migrator = new Migrator("grails-app/migrations/changelog.xml", new CompositeFileOpener(new GrailsFileOpener(),new FileSystemFileOpener()));
        println "Migrator: "+migrator.toString();
        println "Data soruce"+config.dataSource.getClass().getName()
        migrator.init(p.driverClassName, p.url, p.username, p.password, classLoader)
    }
}


