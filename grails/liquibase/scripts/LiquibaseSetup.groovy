import org.codehaus.groovy.grails.compiler.support.*
import java.sql.*;
import liquibase.exception.*;

Ant.property(environment: "env")
grailsHome = Ant.antProject.properties."env.GRAILS_HOME"

includeTargets << new File("${grailsHome}/scripts/Compile.groovy")

config = new ConfigObject()

migrator = null
migratorClass = null;

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

        Driver driver;
        try {
            if (p.driver == null) {
                p.driver = DatabaseFactory.getInstance().findDefaultDriver(p.url);
            }

            if (p.driver == null) {
                throw new RuntimeException("Driver class was not specified and could not be determined from the url");
            }

            driver = (Driver) Class.forName(p.driver, true, classLoader).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot get database driver: " + e.getMessage());
        }
        Properties info = new Properties();
        info.put("user", p.username);
        if (p.password != null) {
            info.put("password", p.password);
        }

        Connection connection = driver.connect(p.url, info);
        if (connection == null) {
            throw new RuntimeException("Connection could not be created to " + p.url + " with driver " + driver.getClass().getName() + ".  Possibly the wrong driver for the given database URL");
        }

        try {
            def fileOpener = classLoader.loadClass("org.liquibase.grails.GrailsFileOpener").getConstructor().newInstance()
            migratorClass = classLoader.loadClass("liquibase.migrator.Migrator")
            println "Mode: "+classLoader.loadClass("liquibase.migrator.Migrator$Mode")
            migrator = migratorClass.getConstructor(String.class, classLoader.loadClass("liquibase.FileOpener")).newInstance("changelog.xml", fileOpener);
        } catch (Exception e){
            e.printStackTrace();
        }
        println "Migrator: "+migrator.toString();
        println "Data soruce"+config.dataSource.getClass().getName()
        migrator.init(connection)
    }
}


