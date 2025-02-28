package liquibase.extension.testing.command

import liquibase.command.core.init.InitProjectCommandStep
import liquibase.command.core.init.InteractivePromptingValueProvider
import liquibase.util.SetupDefaultsFile
import liquibase.Scope
import liquibase.configuration.LiquibaseConfiguration
import liquibase.exception.CommandExecutionException
import liquibase.extension.testing.setup.SetupCleanResources
import liquibase.extension.testing.setup.SetupEnvironmentVariableProvider
import liquibase.util.FileUtil
import liquibase.util.StringUtil
import org.yaml.snakeyaml.Yaml

import java.nio.file.Paths
import java.util.regex.Pattern

CommandTests.define {

    command = ["init", "project"]

    afterMethodInvocation = {
        // If the interactive prompting value provider exists, unregister it after test method execution, so that it does
        // not stay around and pollute future test executions.
        def lbConf = Scope.currentScope.getSingleton(LiquibaseConfiguration)
        def providerOpt = lbConf.getProviders().stream().filter({ vp -> vp instanceof InteractivePromptingValueProvider }).findFirst()
        if (providerOpt.isPresent()) {
            lbConf.unregisterProvider(providerOpt.get())
        }
        return
    }

    signature = """
Short Description: Creates the directory and files needed to run Liquibase commands. Run without any flags on the CLI, or set via Environment variable, etc. will launch an interactive guide to walk users through setting up the necessary project's default and changelog files. This guide can be turned off by setting the 'liquibase.command.init.project.projectGuide=off'
Long Description: NOT SET
Required Args:
  NONE
Optional Args:
  changelogFile (String) Relative or fully qualified path to the changelog file
    Default: example-changelog
  format (String) Format of the project changelog sql|xml|json|yaml|yml
    Default: sql
  keepTempFiles (Boolean) For remote project locations, do not delete temporary project files
    Default: false
  password (String) Password to use to connect to the database
    Default: letmein
    OBFUSCATED
  projectDefaultsFile (String) File with default Liquibase properties
    Default: liquibase.properties
  projectDir (String) Relative or fully qualified path to the directory where the project files will be created
    Default: ./
  projectGuide (Boolean) Allow interactive prompts for init project
    Default: true
  url (String) The JDBC database connection URL
    Default: jdbc:h2:tcp://localhost:9090/mem:dev
    OBFUSCATED
  username (String) Username to use to connect to the database
    Default: dbuser
"""
    run "Happy path", {
        String projectDir = InitProjectCommandStep.INIT_PROJECT_DIR_ARG.getDefaultValue()
        String changelog = InitProjectCommandStep.INIT_CHANGELOG_FILE_ARG.getDefaultValue()
        String format = InitProjectCommandStep.INIT_FORMAT_ARG.getDefaultValue()
        String defaultsFile = InitProjectCommandStep.INIT_DEFAULTS_FILE_ARG.getDefaultValue()
        setup {
            //
            // Create the source changelog and properties file
            // We cleanup source files and the target that was created during the test
            //
            createTempResource("changelogs/init/example-changelog.sql", "examples/sql/example-changelog.sql", "target")
            createTempResource("liquibase.sql.properties", "examples/sql/liquibase.properties", "target")
            cleanResources(
                    "target/examples/sql/example-changelog.sql",
                    "target/examples/sql/liquibase.properties",
                    new File(InitProjectCommandStep.INIT_DEFAULTS_FILE_ARG.getDefaultValue()+".backup.01").getAbsolutePath(),
                    new File(InitProjectCommandStep.INIT_DEFAULTS_FILE_ARG.getDefaultValue()).getAbsolutePath())
            cleanResources(SetupCleanResources.CleanupMode.CLEAN_ON_BOTH,
                    "example-changelog.sql",
                    "liquibase.flowvariables.yaml",
                    "liquibase.flowfile.yaml",
                    "liquibase.advanced.flowfile.yaml",
                    "liquibase.endstage.flow",
                    "liquibase.checks-package.yaml")
        }
        arguments = [
                projectDir: projectDir,
                changelogFile: changelog,
                format: format,
                projectDefaultsFile: defaultsFile,
                url: InitProjectCommandStep.URL_ARG.getDefaultValue(),
                username: InitProjectCommandStep.USERNAME_ARG.getDefaultValue(),
                password: InitProjectCommandStep.PASSWORD_ARG.getDefaultValue(),
        ]
        expectedUI = [
                "Created example changelog file",
                "example-changelog.sql",
                "Created example defaults file",
                "liquibase.properties"
        ]
        expectedResults = [
            statusCode   : 0,
            changelogFile: (Paths.get(projectDir, changelog).toString() + "." + format).replace('\\', '/'),
            usedH2: true,
            projectDefaultsFile: Paths.get(projectDir, defaultsFile)
        ]
        expectFileToExist = new File(".", "example-changelog.sql")
        expectFileToExist = new File(".", "liquibase.properties")
        expectedFileContent = [
                "./liquibase.properties" : [CommandTests.assertContains("example-changelog.sql")]
        ]
    }

    run "Happy path with only some arguments", {
        String projectDir = InitProjectCommandStep.INIT_PROJECT_DIR_ARG.getDefaultValue()
        String changelog = InitProjectCommandStep.INIT_CHANGELOG_FILE_ARG.getDefaultValue()
        String format = InitProjectCommandStep.INIT_FORMAT_ARG.getDefaultValue()
        String defaultsFile = InitProjectCommandStep.INIT_DEFAULTS_FILE_ARG.getDefaultValue()
        setup {
            //
            // Create the source changelog and properties file
            // We cleanup source files and the target that was created during the test
            //
            createTempResource("changelogs/init/example-changelog.sql", "examples/sql/example-changelog.sql", "target")
            createTempResource("liquibase.sql.properties", "examples/sql/liquibase.properties", "target")
            cleanResources(
                    "target/examples/sql/example-changelog.sql",
                    "target/examples/sql/liquibase.properties",
                    "example-changelog.sql",
                    new File(InitProjectCommandStep.INIT_DEFAULTS_FILE_ARG.getDefaultValue()).getAbsolutePath(),
                    "liquibase.flowvariables.yaml",
                    "liquibase.flowfile.yaml",
                    "liquibase.advanced.flowfile.yaml",
                    "liquibase.endstage.flow",
                    "liquibase.checks-package.yaml")
        }
        arguments = [
                projectDir: projectDir,
                changelogFile: changelog,
                format: format,
                projectGuide: "on"
        ]
        expectedUI = [
                "Created example changelog file",
                "example-changelog.sql",
                "Created example defaults file",
                "liquibase.properties",
                "Setup new liquibase.properties"
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: (Paths.get(projectDir, changelog).toString() + "." + format).replace('\\', '/'),
                usedH2: true,
                projectDefaultsFile: Paths.get(projectDir, defaultsFile)
        ]
        expectFileToExist = new File(".", "example-changelog.sql")
        expectFileToExist = new File(".", defaultsFile)
        expectedFileContent = [
                "./liquibase.properties" : [CommandTests.assertContains("example-changelog.sql")]
        ]
    }

    run "Happy path with projectGuide off", {
        String projectDir = InitProjectCommandStep.INIT_PROJECT_DIR_ARG.getDefaultValue()
        String changelog = InitProjectCommandStep.INIT_CHANGELOG_FILE_ARG.getDefaultValue()
        String format = InitProjectCommandStep.INIT_FORMAT_ARG.getDefaultValue()
        String defaultsFile = InitProjectCommandStep.INIT_DEFAULTS_FILE_ARG.getDefaultValue()
        setup {
            //
            // Create the source changelog and properties file
            // We cleanup source files and the target that was created during the test
            //
            createTempResource("changelogs/init/example-changelog.sql", "examples/sql/example-changelog.sql", "target")
            createTempResource("liquibase.sql.properties", "examples/sql/liquibase.properties", "target")
            cleanResources(
                    "target/examples/sql/example-changelog.sql",
                    "target/examples/sql/liquibase.properties",
                    "example-changelog.sql",
                    new File(InitProjectCommandStep.INIT_DEFAULTS_FILE_ARG.getDefaultValue()).getAbsolutePath(),
                    "liquibase.flowvariables.yaml",
                    "liquibase.flowfile.yaml",
                    "liquibase.advanced.flowfile.yaml",
                    "liquibase.endstage.flow",
                    "liquibase.checks-package.yaml")
        }
        arguments = [
                projectDir: projectDir,
                changelogFile: changelog,
                format: format,
                projectGuide: "off"
        ]
        expectedUI = [
                "Created example changelog file",
                "example-changelog.sql",
                "Created example defaults file",
                "liquibase.properties"
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: (Paths.get(projectDir, changelog).toString() + "." + format).replace('\\', '/'),
                usedH2: true,
                projectDefaultsFile: Paths.get(projectDir, defaultsFile)
        ]
        expectFileToExist = new File(".", "example-changelog.sql")
        expectFileToExist = new File(".", defaultsFile)
        expectedFileContent = [
                "./liquibase.properties" : [CommandTests.assertContains("example-changelog.sql")]
        ]
    }

    run "Happy path with projectGuide on to override headless setting", {
        String projectDir = InitProjectCommandStep.INIT_PROJECT_DIR_ARG.getDefaultValue()
        String changelog = InitProjectCommandStep.INIT_CHANGELOG_FILE_ARG.getDefaultValue()
        String format = InitProjectCommandStep.INIT_FORMAT_ARG.getDefaultValue()
        String defaultsFile = InitProjectCommandStep.INIT_DEFAULTS_FILE_ARG.getDefaultValue()
        setup {
            //
            // Create the source changelog and properties file
            // We cleanup source files and the target that was created during the test
            //
            createTempResource("changelogs/init/example-changelog.sql", "examples/sql/example-changelog.sql", "target")
            createTempResource("liquibase.sql.properties", "examples/sql/liquibase.properties", "target")
            cleanResources(
                    "target/examples/sql/example-changelog.sql",
                    "target/examples/sql/liquibase.properties",
                    "example-changelog.sql",
                    new File(InitProjectCommandStep.INIT_DEFAULTS_FILE_ARG.getDefaultValue()).getAbsolutePath(),
                    "liquibase.flowvariables.yaml",
                    "liquibase.flowfile.yaml",
                    "liquibase.advanced.flowfile.yaml",
                    "liquibase.endstage.flow",
                    "liquibase.checks-package.yaml")
            def add = [ LIQUIBASE_STRICT:"true", LIQUIBASE_HEADLESS:"true" ]
            String[] remove = [:]
            run(
                    new SetupEnvironmentVariableProvider(add, remove)
            )
        }
        arguments = [
                projectDir: projectDir,
                changelogFile: changelog,
                format: format,
                projectGuide: "on"
        ]
        expectedUI = [
                "Created example changelog file",
                "example-changelog.sql",
                "Created example defaults file",
                "liquibase.properties",
                "Setup new liquibase.properties"
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: (Paths.get(projectDir, changelog).toString() + "." + format).replace('\\', '/'),
                usedH2: true,
                projectDefaultsFile: Paths.get(projectDir, defaultsFile)
        ]
        expectFileToExist = new File(".", "example-changelog.sql")
        expectFileToExist = new File(".", defaultsFile)
        expectedFileContent = [
                "./liquibase.properties" : [CommandTests.assertContains("example-changelog.sql")]
        ]
    }

    run "Happy path where the format is determined from the extension", {
        String changelog = "my-changelog.xml"
        String projectDirectory = "target/test-classes/projectDirectory"+ StringUtil.randomIdentifer(10)
        String defaultsFile = InitProjectCommandStep.INIT_DEFAULTS_FILE_ARG.getDefaultValue()
        setup {
            //
            // Create the source changelog and properties file
            // We cleanup source files and the target that was created during the test
            //
            createTempResource("liquibase.xml.properties", "examples/xml/liquibase.properties", "target")
            cleanResources("target/examples/xml/example-changelog.xml", "target/examples/xml/liquibase.properties", projectDirectory)
        }
        arguments = [
                projectDir: projectDirectory,
                changelogFile: changelog
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: Paths.get(projectDirectory, changelog).toString().replace('\\', '/'),
                usedH2: true,
                projectDefaultsFile: Paths.get(projectDirectory, defaultsFile)
        ]
        expectFileToExist = new File(projectDirectory, "my-changelog.xml")
        expectFileToNotExist = new File(projectDirectory, "example-changelog.xml")
        expectedFileContent = [
                (projectDirectory + "/liquibase.properties") : [CommandTests.assertContains("my-changelog.xml")]
        ]
    }

    run "Happy path with a non-default project directory", {
        String projectDirectory = "target/test-classes/projectDirectory"+ StringUtil.randomIdentifer(10)
        String defaultsFile = InitProjectCommandStep.INIT_DEFAULTS_FILE_ARG.getDefaultValue()
        setup {
            createTempResource("changelogs/init/example-changelog.sql", "examples/sql/example-changelog.sql", "target")
            createTempResource("liquibase.sql.properties", "examples/sql/liquibase.properties", "target")
            cleanResources("target/examples/sql/example-changelog.sql", "target/examples/sql/liquibase.properties", projectDirectory)
        }
        arguments = [
            projectDir: projectDirectory
        ]
        expectedResults = [
            statusCode   : 0,
            changelogFile: Paths.get(projectDirectory, "example-changelog.sql").toString().replace('\\', '/'),
            usedH2: true,
            projectDefaultsFile: Paths.get(projectDirectory, defaultsFile)
        ]
        expectFileToExist = new File(projectDirectory, "example-changelog.sql")
        expectFileToNotExist = new File(projectDirectory, "liquibase.properties.backup.01")
        expectedFileContent = [
                (projectDirectory + "/liquibase.properties") : [CommandTests.assertContains("example-changelog.sql")]
        ]
    }

    run "Happy path with non-default arguments", {
        String projectDirectory = "target/test-classes/projectDirectory"+ StringUtil.randomIdentifer(10)
        setup {
            createTempResource("changelogs/init/example-changelog.sql", "examples/xml/example-changelog.xml", "target")
            createTempResource("liquibase.xml.properties", "examples/xml/liquibase.properties", "target")
            cleanResources("target/examples/xml/example-changelog.xml", "target/examples/xml/liquibase.properties", projectDirectory)
        }
        arguments = [
            projectDir: projectDirectory,
            projectDefaultsFile: "lb.properties",
            changelogFile: "changelog.xml"
        ]
        expectedResults = [
            statusCode   : 0,
            changelogFile: Paths.get(projectDirectory, "changelog.xml").toString().replace('\\', '/'),
            usedH2: true,
            projectDefaultsFile: Paths.get(projectDirectory, "lb.properties")
        ]
        expectedUI = [
            "Created example changelog file",
            "changelog.xml",
            "Created example defaults file",
            "lb.properties"
        ]
        expectFileToExist = new File(projectDirectory, "changelog.xml")
        expectFileToExist = new File(projectDirectory, "lb.properties")
        expectFileToNotExist = new File(projectDirectory, "lb.properties.backup.01")
        expectedFileContent = [
                (projectDirectory + "/lb.properties") : [CommandTests.assertContains("changelog.xml")]
        ]
    }

    run "Happy path with an XML format", {
        String projectDirectory = "target/test-classes/projectDirectory"+ StringUtil.randomIdentifer(10)
        String defaultsFile = InitProjectCommandStep.INIT_DEFAULTS_FILE_ARG.getDefaultValue()
        setup {
            createTempResource("changelogs/init/example-changelog.xml", "examples/xml/example-changelog.xml", "target")
            createTempResource("liquibase.xml.properties", "examples/xml/liquibase.properties", "target")
            cleanResources("target/examples/xml/example-changelog.xml", "target/examples/xml/liquibase.properties", projectDirectory)
        }
        arguments = [
                projectDir: projectDirectory,
                format: "xml"
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: (Paths.get(projectDirectory, InitProjectCommandStep.INIT_CHANGELOG_FILE_ARG.getDefaultValue()).toString() + ".xml").replace('\\', '/'),
                usedH2: true,
                projectDefaultsFile: Paths.get(projectDirectory, defaultsFile)
        ]
        expectedUI = [
                "Created example changelog file",
                "example-changelog.xml",
                "Created example defaults file",
                "liquibase.properties"
        ]
        expectFileToExist = new File(projectDirectory, "example-changelog.xml")
        expectedFileContent = [
                (projectDirectory + "/liquibase.properties") : [CommandTests.assertContains("example-changelog.xml")]
        ]
    }

    run "Happy path with a JSON format", {
        String projectDirectory = "target/test-classes/projectDirectory"
        String defaultsFile = InitProjectCommandStep.INIT_DEFAULTS_FILE_ARG.getDefaultValue()
        setup {
            createTempResource("changelogs/init/example-changelog.json", "examples/json/example-changelog.json", "target")
            createTempResource("liquibase.yaml.properties", "examples/json/liquibase.properties", "target")
            cleanResources("target/examples/json/example-changelog.json", "target/examples/json/liquibase.properties", projectDirectory)
        }
        arguments = [
                projectDir: projectDirectory,
                format: "json"
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: (Paths.get(projectDirectory, InitProjectCommandStep.INIT_CHANGELOG_FILE_ARG.getDefaultValue()).toString() + ".json").replace('\\', '/'),
                usedH2: true,
                projectDefaultsFile: Paths.get(projectDirectory, defaultsFile)
        ]
        expectFileToExist = new File("target/test-classes/projectDirectory/example-changelog.json")
        expectFileToExist = new File("target/test-classes/projectDirectory/liquibase.properties")
        expectedFileContent = [
                "target/test-classes/projectDirectory/liquibase.properties" : [CommandTests.assertContains("example-changelog.json")]
        ]
        expectations = {
            Yaml yaml = new Yaml()
            yaml.load(FileUtil.getContents(new File("target/test-classes/projectDirectory/example-changelog.json")))
            return null
        }
    }

    run "Happy path with a YAML format", {
        String projectDirectory = "target/test-classes/projectDirectory"
        String defaultsFile = InitProjectCommandStep.INIT_DEFAULTS_FILE_ARG.getDefaultValue()
        setup {
            createTempResource("changelogs/init/example-changelog.yaml", "examples/yaml/example-changelog.yaml", "target")
            createTempResource("liquibase.yaml.properties", "examples/yaml/liquibase.properties", "target")
            cleanResources("target/examples/yaml/example-changelog.yaml", "target/examples/yaml/liquibase.properties", projectDirectory)
        }
        arguments = [
                projectDir: projectDirectory,
                format: "yaml"
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: (Paths.get(projectDirectory, InitProjectCommandStep.INIT_CHANGELOG_FILE_ARG.getDefaultValue()).toString() + ".yaml").replace('\\', '/'),
                usedH2: true,
                projectDefaultsFile: Paths.get(projectDirectory, defaultsFile)
        ]
        expectFileToExist = new File("target/test-classes/projectDirectory/example-changelog.yaml")
        expectFileToExist = new File("target/test-classes/projectDirectory/liquibase.properties")
        expectFileToNotExist = new File("target/test-classes/projectDirectory/example-changelog.sql")
        expectedFileContent = [
                "target/test-classes/projectDirectory/liquibase.properties" : [CommandTests.assertContains("example-changelog.yaml")]
        ]
        expectations = {
            Yaml yaml = new Yaml()
            yaml.load(FileUtil.getContents(new File("target/test-classes/projectDirectory/example-changelog.yaml")))
            return null
        }
    }

    run "Happy path with a YML format", {
        String projectDirectory = "target/test-classes/projectDirectory"
        String defaultsFile = InitProjectCommandStep.INIT_DEFAULTS_FILE_ARG.getDefaultValue()
        setup {
            createTempResource("changelogs/init/example-changelog.yaml", "examples/yaml/example-changelog.yaml", "target")
            createTempResource("liquibase.yaml.properties", "examples/yaml/liquibase.properties", "target")
            cleanResources("target/examples/yaml/example-changelog.yaml", "target/examples/yaml/liquibase.properties", projectDirectory)
        }
        arguments = [
                projectDir: projectDirectory,
                format: "yml"
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: (Paths.get(projectDirectory, InitProjectCommandStep.INIT_CHANGELOG_FILE_ARG.getDefaultValue()).toString() + ".yml").replace('\\', '/'),
                usedH2: true,
                projectDefaultsFile: Paths.get(projectDirectory, defaultsFile)
        ]
        expectFileToExist = new File("target/test-classes/projectDirectory/example-changelog.yml")
        expectFileToExist = new File("target/test-classes/projectDirectory/liquibase.properties")
        expectFileToNotExist = new File("target/test-classes/projectDirectory/example-changelog.sql")
        expectedFileContent = [
                "target/test-classes/projectDirectory/liquibase.properties" : [CommandTests.assertContains("example-changelog.yml")]
        ]
    }

    run "Happy path with a specified properties file", {
        String projectDirectory = "target/test-classes/projectDirectory"
        String defaultsFile = "liquibase.new.properties"
        setup {
            createTempResource("changelogs/init/example-changelog.sql", "examples/sql/example-changelog.sql", "target")
            createTempResource("liquibase.yaml.properties", "examples/sql/liquibase.properties", "target")
            cleanResources("target/examples/sql/example-changelog.sql", "examples/sql/liquibase.properties", projectDirectory)
        }
        arguments = [
                projectDir: projectDirectory,
                projectDefaultsFile: defaultsFile
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: (Paths.get(projectDirectory, InitProjectCommandStep.INIT_CHANGELOG_FILE_ARG.getDefaultValue()).toString() + ".sql").replace('\\', '/'),
                usedH2: true,
                projectDefaultsFile: Paths.get(projectDirectory, defaultsFile)
        ]
        expectFileToExist = new File("target/test-classes/projectDirectory/liquibase.new.properties")
        expectedFileContent = [
                "target/test-classes/projectDirectory/liquibase.new.properties" : [CommandTests.assertContains("example-changelog.sql")]
        ]
    }

    run "Happy path with URL/username/password arguments", {
        String projectDirectory = "target/test-classes/projectDirectory"
        String defaultsFile = InitProjectCommandStep.INIT_DEFAULTS_FILE_ARG.getDefaultValue()
        setup {
            createTempResource("changelogs/init/example-changelog.sql", "examples/sql/example-changelog.sql", "target")
            createTempResource("liquibase.sample.properties", "examples/sql/liquibase.properties", "target")
            cleanResources("target/examples/sql/example-changelog.sql", "target/examples/sql/liquibase.properties", projectDirectory)
        }
        arguments = [
            projectDir: projectDirectory,
            url: "jdbc:oracle:thin@localhost:1521/BUCKET_01",
            username: "bad_dude",
            password: "simple_password"
        ]
        expectedResults = [
            statusCode   : 0,
            changelogFile: (Paths.get(projectDirectory, InitProjectCommandStep.INIT_CHANGELOG_FILE_ARG.getDefaultValue()).toString() + ".sql").replace('\\', '/'),
            usedH2: false,
            projectDefaultsFile: Paths.get(projectDirectory, defaultsFile)
        ]
        //
        // Look for the specific values
        //
        expectedFileContent = [
                "target/test-classes/projectDirectory/liquibase.properties" : [
                        CommandTests.assertContains("jdbc:oracle:thin@localhost:1521/BUCKET_01"),
                        CommandTests.assertContains("bad_dude"),
                        CommandTests.assertContains("simple_password")
                ]
        ]
    }

    run "Changelog file without an extension", {
        String projectDirectory = "target/test-classes/projectDirectory"
        String defaultsFile = InitProjectCommandStep.INIT_DEFAULTS_FILE_ARG.getDefaultValue()
        arguments = [
                projectDir: projectDirectory,
                changelogFile: "simple-changelog",
                format: "xml"
        ]
        setup {
            createTempResource("changelogs/init/example-changelog.xml", "examples/xml/example-changelog.xml", "target")
            createTempResource("liquibase.yaml.properties", "liquibase.properties", projectDirectory)
            cleanResources("${projectDirectory}/simple-changelog.xml", "${projectDirectory}/liquibase.properties", projectDirectory)
        }
        expectFileToExist = new File("target/test-classes/projectDirectory/simple-changelog.xml")
        expectedFileContent = [
                "target/test-classes/projectDirectory/liquibase.properties" : [CommandTests.assertContains("simple-changelog.xml")]
        ]
        expectedResults = [
                projectDefaultsFile: Paths.get(projectDirectory, defaultsFile),
                changelogFile: Paths.get(projectDirectory, "simple-changelog.xml").toString().replace('\\', '/'),
                statusCode: 0,
                usedH2: true
        ]
    }

    run "Use the existing defaults file if it exists", {
        String projectDirectory = "target/test-classes/projectDirectory"
        String defaultsFile = InitProjectCommandStep.INIT_DEFAULTS_FILE_ARG.getDefaultValue()
        arguments = [
                projectDir: projectDirectory,
                url: "jdbc:oracle:thin@localhost:1521/BUCKET_01"
        ]
        setup {
            createTempResource("changelogs/init/example-changelog.sql", "simple.changelog.sql", projectDirectory )
            createTempResource("liquibase.xml.properties", "liquibase.properties", projectDirectory)
            cleanResources("${projectDirectory}/simple.changelog.sql", "${projectDirectory}/liquibase.properties", projectDirectory)
        }
        expectFileToExist = new File(projectDirectory, "liquibase.properties.backup.01")
        expectedUI = Pattern.compile("The defaults file .*liquibase.properties. was backed up and then updated with your supplied values.")
        expectedFileContent = [
                "target/test-classes/projectDirectory/liquibase.properties" : [CommandTests.assertContains("jdbc:oracle:thin@localhost:1521/BUCKET_01")]
        ]
        expectedResults = [
                projectDefaultsFile: Paths.get(projectDirectory, defaultsFile),
                changelogFile: Paths.get(projectDirectory, "example-changelog.sql").toString().replace('\\', '/'),
                statusCode: 0,
                usedH2: false
        ]
    }

    run "Project directory already exists", {
        String projectDirectory = "target/test-classes/projectDirectory"+ StringUtil.randomIdentifer(10)
        String defaultsFile = InitProjectCommandStep.INIT_DEFAULTS_FILE_ARG.getDefaultValue()
        setup {
            createTempDirectoryResource(projectDirectory)
            createTempResource("changelogs/init/example-changelog.sql", "examples/sql/example-changelog.sql", "target")
            createTempResource("liquibase.yaml.properties", "examples/sql/liquibase.properties", "target")
            cleanResources("target/examples/sql/example-changelog.sql", "target/examples/sql/liquibase.properties", projectDirectory)
        }
        arguments = [
                projectDir: projectDirectory,
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: (Paths.get(projectDirectory, InitProjectCommandStep.INIT_CHANGELOG_FILE_ARG.getDefaultValue()).toString() + ".sql").replace('\\', '/'),
                usedH2: true,
                projectDefaultsFile: Paths.get(projectDirectory, defaultsFile)
        ]
        expectFileToExist = new File(projectDirectory, "liquibase.properties")
        expectFileToExist = new File(projectDirectory, "example-changelog.sql")
    }

   run "Changelog file exists in the project directory", {
       String projectDirectory = "target/test-classes/projectDirectory"
       String defaultsFile = InitProjectCommandStep.INIT_DEFAULTS_FILE_ARG.getDefaultValue()
       setup {
           createTempDirectoryResource(projectDirectory)
           createTempResource("changelogs/init/example-changelog.sql", "my-changelog.sql", "target/test-classes/projectDirectory")
           createTempResource("liquibase.yaml.properties", "examples/sql/liquibase.properties", "target")
           cleanResources("target/examples/sql/liquibase.properties", projectDirectory)
       }
       arguments = [
           projectDir: projectDirectory,
           changelogFile: "my-changelog.sql"
       ]
       testUI = new CommandTests.TestUIWithAnswers(["y"] as String[])
       expectedUI = [
           "Setup new liquibase.properties, flowfile, and sample changelog? Enter (Y)es with defaults, yes with (C)ustomization, or (N)o. [Y]:",
           "Setting up new Liquibase project in '",
           "Created example defaults file '",
           "liquibase.properties'",
           "To use the new project files, please cd into '"
       ]
       expectedResults = [
           statusCode   : 0,
           changelogFile: Paths.get(projectDirectory, "my-changelog.sql").toString().replace('\\', '/'),
           usedH2: true,
           projectDefaultsFile: Paths.get(projectDirectory, defaultsFile)
       ]
       expectFileToExist = new File(projectDirectory, "liquibase.properties")
       expectFileToExist = new File(projectDirectory, "my-changelog.sql")
    }

    run "Defaults file exists in the project directory", {
        String projectDirectory = "target/test-classes/projectDirectory" + StringUtil.randomIdentifer(10)
        String defaultsFile = InitProjectCommandStep.INIT_DEFAULTS_FILE_ARG.getDefaultValue()
        setup {
            createTempDirectoryResource(projectDirectory)
            createTempResource("changelogs/init/example-changelog.sql", "examples/sql/example-changelog.sql", "target")
            createTempResource("liquibase.yaml.properties", "liquibase.properties", projectDirectory)
            cleanResources("target/examples/sql/liquibase.properties", projectDirectory)
        }
        arguments = [
                projectDir: projectDirectory
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: (Paths.get(projectDirectory, InitProjectCommandStep.INIT_CHANGELOG_FILE_ARG.getDefaultValue()).toString() + ".sql").replace('\\', '/'),
                usedH2: true,
                projectDefaultsFile: Paths.get(projectDirectory, defaultsFile)
        ]
        expectFileToExist = new File(projectDirectory, "liquibase.properties")
        expectFileToExist = new File(projectDirectory, "example-changelog.sql")
    }

    run "Error if changelog file has path elements", {
        String projectDirectory = "target/test-classes/projectDirectory" + StringUtil.randomIdentifer(10)
        setup {
            createTempResource("changelogs/init/example-changelog.sql", "examples/sql/example-changelog.sql", "target")
            createTempResource("liquibase.yaml.properties", "examples/sql/liquibase.properties", "target")
            cleanResources("target/examples/sql/example-changelog.sql", "target/examples/sql/liquibase.properties", projectDirectory)
        }
        arguments = [
                projectDir: projectDirectory,
                changelogFile: "hsqldb/complete/rollback.tag.changelog.xml"
        ]
        expectedException = CommandExecutionException.class
        expectedExceptionMessage = "java.lang.IllegalArgumentException: Filename cannot contain path elements."
    }

    run "Error if changelog file has Mac path elements", {
        String projectDirectory = "target/test-classes/projectDirectory"
        setup {
            createTempResource("changelogs/init/example-changelog.sql", "examples/sql/example-changelog.sql", "target")
            createTempResource("liquibase.yaml.properties", "examples/sql/liquibase.properties", "target")
            cleanResources("target/examples/sql/example-changelog.sql", "target/examples/sql/liquibase.properties", projectDirectory)
        }
        arguments = [
                projectDir: projectDirectory,
                changelogFile: "\\User\\changelog.sql"
        ]
        expectedException = CommandExecutionException.class
        expectedExceptionMessage = "java.lang.IllegalArgumentException: Filename cannot contain path elements."
    }

    run "Error if project directory is a file", {
        arguments = [
                projectDir: "target/test-classes/two-tables.xml"
        ]
        setup {
            createTempResource("changelogs/init/example-changelog.xml", "two-tables.xml")
            cleanResources("two-tables.xml")
        }
        expectedException = CommandExecutionException.class
        expectedExceptionMessage =
           Pattern.compile("The specified project directory .*target.test-classes.two-tables.xml.* cannot be a file", Pattern.MULTILINE | Pattern.DOTALL);
    }

    run "Use the existing files if both properties and changelog files exist", {
        String projectDirectory = "target/test-classes/projectDirectory"
        String defaultsFile = InitProjectCommandStep.INIT_DEFAULTS_FILE_ARG.getDefaultValue()
        arguments = [
                projectDir: projectDirectory,
                changelogFile: "simple.changelog.sql",
                url: "jdbc:oracle:thin@localhost:1521/BUCKET_01"
        ]
        setup {
            createTempResource("changelogs/init/example-changelog.sql", "simple.changelog.sql", projectDirectory )
            createTempResource("liquibase.yaml.properties", "liquibase.properties", projectDirectory)
            cleanResources("${projectDirectory}/simple.changelog.sql", "${projectDirectory}/liquibase.properties", projectDirectory)
        }
        expectedFileContent = [
            "target/test-classes/projectDirectory/liquibase.properties" : [CommandTests.assertContains("jdbc:oracle:thin@localhost:1521/BUCKET_01")]
        ]
        expectedUI = [
            "For more details, visit the Getting Started Guide at https://docs.liquibase.com/start/home.html"
        ]
        expectedResults = [
                projectDefaultsFile: Paths.get(projectDirectory, defaultsFile),
                changelogFile: Paths.get(projectDirectory, "simple.changelog.sql").toString().replace('\\', '/'),
                statusCode: 0,
                usedH2: false
        ]
    }

    run "Interactive happy path taking defaults, non default directory", {
        String projectDirectory = "target/test-classes/" + StringUtil.randomIdentifier(10)
        String defaultsFile = InitProjectCommandStep.INIT_DEFAULTS_FILE_ARG.getDefaultValue()
        arguments = [
                projectDir: projectDirectory
        ]
        setup {
            createTempResource("changelogs/init/example-changelog.sql", "examples/sql/example-changelog.sql", "target")
            createTempResource("liquibase.yaml.properties", "examples/sql/liquibase.properties", "target")
            cleanResources("target/examples/sql/example-changelog.sql", "target/examples/sql/liquibase.properties", "${projectDirectory}/example-changelog.sql", "${projectDirectory}/liquibase.properties")
        }
        testUI = new CommandTests.TestUIWithAnswers(["y"] as String[])
        expectedUI = [
                "Setup new liquibase.properties, flowfile, and sample changelog? Enter (Y)es with defaults, yes with (C)ustomization, or (N)o. [Y]:",
                "Setting up new Liquibase project in '",
                "Created example changelog file '", // removed the middle part of the absolute path here
                "example-changelog.sql'",
                "Created example defaults file '",
                "liquibase.properties'",
                "To use the new project files, please cd into '${projectDirectory}', make sure your database is active and accessible by opening a new terminal window to run \"liquibase init start-h2\", and then return to this terminal window to run \"liquibase update\" command. For more details, visit the Getting Started Guide at https://docs.liquibase.com/start/home.html"
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: (Paths.get(projectDirectory, InitProjectCommandStep.INIT_CHANGELOG_FILE_ARG.getDefaultValue()).toString() + ".sql").replace('\\', '/'),
                usedH2: true,
                projectDefaultsFile: Paths.get(projectDirectory, defaultsFile)
        ]
    }

    run "Interactive happy path taking defaults by going through customization flow, non default directory", {
        String projectDirectory = "target/test-classes/" + StringUtil.randomIdentifier(10)
        String defaultsFile = InitProjectCommandStep.INIT_DEFAULTS_FILE_ARG.getDefaultValue()
        arguments = [
                projectDir: projectDirectory
        ]
        setup {
            createTempResource("changelogs/init/example-changelog.sql", "examples/sql/example-changelog.sql", "target")
            createTempResource("liquibase.yaml.properties", "examples/sql/liquibase.properties", "target")
            cleanResources("target/examples/sql/example-changelog.sql", "target/examples/sql/liquibase.properties", "${projectDirectory}/example-changelog.sql", "${projectDirectory}/liquibase.properties")
        }
        testUI = new CommandTests.TestUIWithAnswers(["c", "", "", "", "", "", ""] as String[])
        expectedUI = [
                """
Setup new liquibase.properties, flowfile, and sample changelog? Enter (Y)es with defaults, yes with (C)ustomization, or (N)o. [Y]: 
Enter name for sample changelog file to be created or (s)kip [example-changelog]: 
Enter your preferred changelog format (options: sql, xml, json, yml, yaml) [sql]: 
Enter name for defaults file to be created or (s)kip [liquibase.properties]: 
Enter the JDBC url without username or password to be used (What is a JDBC url? <url>) [jdbc:h2:tcp://localhost:9090/mem:dev]: 
Enter username to connect to JDBC url [dbuser]: 
Enter password to connect to JDBC url [letmein]: """,
                "Setting up new Liquibase project in '",
                "Created example changelog file '", // removed the middle part of the absolute path here
                "example-changelog.sql'",
                "Created example defaults file '",
                "liquibase.properties'",
                "To use the new project files, please cd into '${projectDirectory}', make sure your database is active and accessible by opening a new terminal window to run \"liquibase init start-h2\", and then return to this terminal window to run \"liquibase update\" command. For more details, visit the Getting Started Guide at https://docs.liquibase.com/start/home.html"
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: (Paths.get(projectDirectory, InitProjectCommandStep.INIT_CHANGELOG_FILE_ARG.getDefaultValue()).toString() + ".sql").replace('\\', '/'),
                usedH2: true,
                projectDefaultsFile: Paths.get(projectDirectory, defaultsFile)
        ]
    }

    run "Interactive happy path taking defaults, with default directory", {
        String projectsDefaultsFile = "liquibase" + StringUtil.randomIdentifer(10) + ".properties"
        arguments = [
                projectDefaultsFile: projectsDefaultsFile // we specify a project defaults file parameter here because we need to make sure that we have a unique name that won't conflict with any existing files
        ]
        setup {
            createTempResource("changelogs/init/example-changelog.sql", "examples/sql/example-changelog.sql")
            createTempResource("liquibase.yaml.properties", "examples/sql/liquibase.properties")
            cleanResources("target/examples/sql/example-changelog.sql", "target/examples/sql/liquibase.properties", "example-changelog.sql", projectsDefaultsFile, new File(InitProjectCommandStep.INIT_DEFAULTS_FILE_ARG.getDefaultValue()).getAbsolutePath(),
                    "liquibase.flowvariables.yaml",
                    "liquibase.flowfile.yaml",
                    "liquibase.advanced.flowfile.yaml",
                    "liquibase.endstage.flow",
                    "liquibase.checks-package.yaml")
        }
        testUI = new CommandTests.TestUIWithAnswers(["y"] as String[])
        expectedUI = [
                "Setup new liquibase.properties, flowfile, and sample changelog? Enter (Y)es with defaults, yes with (C)ustomization, or (N)o. [Y]:",
                "Setting up new Liquibase project in '",
                "Created example changelog file '", // removed the middle part of the absolute path here
                "example-changelog.sql'",
                "Created example defaults file '",
                projectsDefaultsFile + "'",
                "To use the new project files make sure your database is active and accessible by opening a new terminal window to run \"liquibase init start-h2\", and then return to this terminal window to run \"liquibase update --defaults-file="+projectsDefaultsFile+"\" command. For more details, visit the Getting Started Guide at https://docs.liquibase.com/start/home.html"
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: (Paths.get(InitProjectCommandStep.INIT_PROJECT_DIR_ARG.getDefaultValue(), InitProjectCommandStep.INIT_CHANGELOG_FILE_ARG.getDefaultValue()).toString() + ".sql").replace('\\', '/'),
                usedH2: true,
                projectDefaultsFile: Paths.get(InitProjectCommandStep.INIT_PROJECT_DIR_ARG.getDefaultValue(), projectsDefaultsFile)
        ]
    }

    run "Interactive path with no arguments", {
        testUI = new CommandTests.TestUIWithAnswers(["n"] as String[])
        expectedUI = """Setup new liquibase.properties, flowfile, and sample changelog? Enter (Y)es with defaults, yes with (C)ustomization, or (N)o. [Y]: 
No files created. Set 'liquibase.command.init.project.projectGuide=off' in your defaults file or set LIQUIBASE_COMMAND_INIT_PROJECT_PROJECT_GUIDE=off as an environment variable to not be asked again. Getting Started and project setup available anytime, run "liquibase init project --help" for information."""

    }

    run "Interactive with custom path", {
        String defaultsFile = "banana.properties"
        setup {
            createTempResource("changelogs/init/example-changelog.xml", "examples/xml/example-changelog.xml", "target")
            createTempResource("liquibase.yaml.properties", "examples/sql/liquibase.properties", "target")
            cleanResources("target/examples/xml/example-changelog.xml", "target/examples/sql/liquibase.properties", "newdir")
        }
        testUI = new CommandTests.TestUIWithAnswers(["c","newdir", "hello.changelog.sql",  defaultsFile, "jdbcurl", "username", "password"] as String[])
        expectedUI = [
                """Setup new liquibase.properties, flowfile, and sample changelog? Enter (Y)es with defaults, yes with (C)ustomization, or (N)o. [Y]:
Enter a relative path to desired project directory [./]: 
Enter name for sample changelog file to be created or (s)kip [example-changelog]: 
Enter name for defaults file to be created or (s)kip [liquibase.properties]: 
Enter the JDBC url without username or password to be used (What is a JDBC url? <url>) [jdbc:h2:tcp://localhost:9090/mem:dev]: 
Enter username to connect to JDBC url [dbuser]: 
Enter password to connect to JDBC url [letmein]: 
Setting up new Liquibase project in '""","newdir",
                "Created example changelog file '",
                "newdir","hello.changelog.sql'",
                "Created example defaults file '",
                "newdir","banana.properties'",
                "To use the new project files, please cd into 'newdir', make sure your database is active and accessible and run \"liquibase update --defaults-file=banana.properties\". For more details, visit the Getting Started Guide at https://docs.liquibase.com/start/home.html"
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: Paths.get("newdir", "hello.changelog.sql").toString().replace('\\', '/'),
                usedH2: false,
                projectDefaultsFile: Paths.get("newdir", defaultsFile)
        ]
        expectedFileContent = [
                "newdir/banana.properties" : [
                        CommandTests.assertContains("liquibase.command.url=jdbcurl"),
                        CommandTests.assertNotContains("liquibase.command.=jdbcurl")
                ]
        ]
    }

    run "Interactive with no extension provided for the changelog file", {
        String defaultsFile = "banana.properties"
        setup {
            createTempResource("changelogs/init/example-changelog.xml", "examples/xml/example-changelog.xml", "target")
            createTempResource("liquibase.yaml.properties", "examples/sql/liquibase.properties", "target")
            cleanResources("target/examples/xml/example-changelog.xml", "target/examples/sql/liquibase.properties", "newdir")
        }
        testUI = new CommandTests.TestUIWithAnswers(["c","newdir", "hellochangelog", "xml",  defaultsFile, "jdbcurl", "username", "password"] as String[])
        expectedUI = [
                """Setup new liquibase.properties, flowfile, and sample changelog? Enter (Y)es with defaults, yes with (C)ustomization, or (N)o. [Y]:
Enter a relative path to desired project directory [./]: 
Enter name for sample changelog file to be created or (s)kip [example-changelog]: 
Enter your preferred changelog format (options: sql, xml, json, yml, yaml) [sql]: 
Enter name for defaults file to be created or (s)kip [liquibase.properties]: 
Enter the JDBC url without username or password to be used (What is a JDBC url? <url>) [jdbc:h2:tcp://localhost:9090/mem:dev]: 
Enter username to connect to JDBC url [dbuser]: 
Enter password to connect to JDBC url [letmein]: 
Setting up new Liquibase project in '""","newdir",
                "Created example changelog file '",
                "newdir","hellochangelog.xml'",
                "Created example defaults file '",
                "newdir","banana.properties'",
                "To use the new project files, please cd into 'newdir', make sure your database is active and accessible and run \"liquibase update --defaults-file=banana.properties\". For more details, visit the Getting Started Guide at https://docs.liquibase.com/start/home.html"
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: Paths.get("newdir", "hellochangelog.xml").toString().replace('\\', '/'),
                usedH2: false,
                projectDefaultsFile: Paths.get("newdir", defaultsFile)
        ]
    }

    run "Interactive with no extension provided for the changelog file and uppercase format", {
        String defaultsFile = "banana.properties"
        setup {
            createTempResource("changelogs/init/example-changelog.xml", "examples/xml/example-changelog.xml", "target")
            createTempResource("liquibase.yaml.properties", "examples/sql/liquibase.properties", "target")
            cleanResources("target/examples/xml/example-changelog.xml", "target/examples/sql/liquibase.properties", "newdir")
        }
        testUI = new CommandTests.TestUIWithAnswers(["c","newdir", "hellochangelog", "XML",  defaultsFile, "jdbcurl", "username", "password"] as String[])
        expectedUI = [
                """Setup new liquibase.properties, flowfile, and sample changelog? Enter (Y)es with defaults, yes with (C)ustomization, or (N)o. [Y]:
Enter a relative path to desired project directory [./]: 
Enter name for sample changelog file to be created or (s)kip [example-changelog]: 
Enter your preferred changelog format (options: sql, xml, json, yml, yaml) [sql]: 
Enter name for defaults file to be created or (s)kip [liquibase.properties]: 
Enter the JDBC url without username or password to be used (What is a JDBC url? <url>) [jdbc:h2:tcp://localhost:9090/mem:dev]: 
Enter username to connect to JDBC url [dbuser]: 
Enter password to connect to JDBC url [letmein]: 
Setting up new Liquibase project in '""","newdir",
                "Created example changelog file '",
                "newdir","hellochangelog.xml'",
                "Created example defaults file '",
                "newdir","banana.properties'",
                "To use the new project files, please cd into 'newdir', make sure your database is active and accessible and run \"liquibase update --defaults-file=banana.properties\". For more details, visit the Getting Started Guide at https://docs.liquibase.com/start/home.html"
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: Paths.get("newdir", "hellochangelog.xml").toString().replace('\\', '/'),
                usedH2: false,
                projectDefaultsFile: Paths.get("newdir", defaultsFile)
        ]
        expectedFileContent = [
                "./newdir/banana.properties" : [CommandTests.assertContains("hellochangelog.xml")]
        ]
    }

    run "Interactive with existing liquibase.properties file", {
        String defaultsFile = "banana.properties"
        setup {
            createTempResource("changelogs/init/example-changelog.xml", "examples/xml/example-changelog.xml", "target")
            createTempResource("liquibase.yaml.properties", "examples/sql/liquibase.properties", "target")
            Properties properties = new Properties()
            properties.put("liquibase.command.url", "jdbc:h2:tcp://localhost:9090/mem:dev")
            properties.put("liquibase.command.username", "dbuser")
            properties.put("liquibase.command.password", "letmein")
            run(new SetupDefaultsFile("liquibase.properties", properties))
            cleanResourcesAfter(new FilenameFilter() {
                @Override
                boolean accept(File file, String s) {
                    return s.startsWith("banana")
                }
            }, new File("."))
            cleanResources(
                    "target/examples/xml/example-changelog.xml",
                    "target/examples/sql/liquibase.properties",
                    new File(defaultsFile).getAbsolutePath(),
                    new File("hello.changelog.sql").getAbsolutePath(),
                    "liquibase.flowvariables.yaml",
                    "liquibase.flowfile.yaml",
                    "liquibase.advanced.flowfile.yaml",
                    "liquibase.endstage.flow",
                    "liquibase.checks-package.yaml")
        }
        testUI = new CommandTests.TestUIWithAnswers(["c","", "hello.changelog.sql",  defaultsFile, "jdbcurl", "username", "password"] as String[])
        expectedUI = [
                """Setup new liquibase.properties, flowfile, and sample changelog? Enter (Y)es with defaults, yes with (C)ustomization, or (N)o. [Y]:
Enter a relative path to desired project directory [./]:
Enter name for sample changelog file to be created or (s)kip [example-changelog]:
Enter name for defaults file to be created or (s)kip [liquibase.properties]:
Enter the JDBC url without username or password to be used (What is a JDBC url? <url>) [jdbc:h2:tcp://localhost:9090/mem:dev]:
Enter username to connect to JDBC url [dbuser]:
Enter password to connect to JDBC url [letmein]:
Setting up new Liquibase project in '"""
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: Paths.get(InitProjectCommandStep.INIT_PROJECT_DIR_ARG.getDefaultValue(), "hello.changelog.sql").toString().replace('\\', '/'),
                usedH2: false,
                projectDefaultsFile: Paths.get(InitProjectCommandStep.INIT_PROJECT_DIR_ARG.getDefaultValue(), defaultsFile)
        ]
    }

    run "Interactive with no extension provided for the changelog file and invalid format", {
        String defaultsFile = "banana.properties"
        setup {
            createTempResource("changelogs/init/example-changelog.xml", "examples/xml/example-changelog.xml", "target")
            createTempResource("liquibase.yaml.properties", "examples/sql/liquibase.properties", "target")
            cleanResources("target/examples/xml/example-changelog.xml", "target/examples/sql/liquibase.properties", "newdir")
        }
        testUI = new CommandTests.TestUIWithAnswers(["c","newdir", "hellochangelog", "foo", "xml", defaultsFile, "jdbcurl", "username", "password"] as String[])
        expectedUI = [
                """Setup new liquibase.properties, flowfile, and sample changelog? Enter (Y)es with defaults, yes with (C)ustomization, or (N)o. [Y]:
Enter a relative path to desired project directory [./]: 
Enter name for sample changelog file to be created or (s)kip [example-changelog]: 
Enter your preferred changelog format (options: sql, xml, json, yml, yaml) [sql]: 
Invalid value: 'foo': Could not find matching value. Valid options include: 'sql', 'xml', 'json', 'yml', 'yaml'.
Enter your preferred changelog format (options: sql, xml, json, yml, yaml) [sql]: 
Enter name for defaults file to be created or (s)kip [liquibase.properties]: 
Enter the JDBC url without username or password to be used (What is a JDBC url? <url>) [jdbc:h2:tcp://localhost:9090/mem:dev]: 
Enter username to connect to JDBC url [dbuser]: 
Enter password to connect to JDBC url [letmein]: 
Setting up new Liquibase project in '""","newdir",
                "Created example changelog file '",
                "newdir","hellochangelog.xml'",
                "Created example defaults file '",
                "newdir","banana.properties'",
                "To use the new project files, please cd into 'newdir', make sure your database is active and accessible and run \"liquibase update --defaults-file=banana.properties\". For more details, visit the Getting Started Guide at https://docs.liquibase.com/start/home.html"
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: Paths.get("newdir", "hellochangelog.xml").toString().replace('\\', '/'),
                usedH2: false,
                projectDefaultsFile: Paths.get("newdir", defaultsFile)
        ]
        expectedFileContent = [
                "./newdir/banana.properties" : [CommandTests.assertContains("hellochangelog.xml")]
        ]
    }

    run "Interactive with existing liquibase.properties file with commented changelogfile", {
        String projectDirectory = "target/test-classes/projectDirectory"+ StringUtil.randomIdentifer(10)
        arguments = [
                projectDir: projectDirectory
        ]
        setup {
            createTempResource("liquibase.yaml.properties", "liquibase.properties", projectDirectory)
            createTempResource("changelogs/init/example-changelog.sql", "example-changelog.sql", projectDirectory)
            cleanResources(projectDirectory)
        }
        testUI = new CommandTests.TestUIWithAnswers(["y"] as String[])
        expectedUI = [
                """Setup new liquibase.properties, flowfile, and sample changelog? Enter (Y)es with defaults, yes with (C)ustomization, or (N)o. [Y]: 
Setting up new Liquibase project in""","To use the new project files, please cd into ",", make sure your database is active and accessible by opening a new terminal window to run \"liquibase init start-h2\", and then return to this terminal window to run \"liquibase update\" command. For more details, visit the Getting Started Guide at https://docs.liquibase.com/start/home.html"
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: Paths.get(projectDirectory, "example-changelog.sql").toString().replace('\\', '/'),
                usedH2: true,
                projectDefaultsFile: Paths.get(projectDirectory, "liquibase.properties")
        ]
        expectedFileContent = [
                (projectDirectory + "/liquibase.properties") : [
                        CommandTests.assertNotContains("liquibase.command.changeLogFile=")
                ]
        ]
    }


    run "Interactive with project dir argument", {
        String defaultsFile = "banana.properties"
        String directory = "projectDirectory" + StringUtil.randomIdentifer(10)
        String projectDirectory = "target/test-classes/" + directory
        setup {
            createTempResource("changelogs/init/example-changelog.xml", "examples/xml/example-changelog.xml", "target")
            createTempResource("liquibase.yaml.properties", "examples/sql/liquibase.properties", "target")
            cleanResources("target/examples/xml/example-changelog.xml", "target/examples/sql/liquibase.properties", projectDirectory)
        }
        arguments = [
                projectDir: projectDirectory
        ]
        testUI = new CommandTests.TestUIWithAnswers(["c", "hello.changelog.sql",  defaultsFile, "jdbcurl", "username", "password"] as String[])
        expectedUI = [
                """Setup new liquibase.properties, flowfile, and sample changelog? Enter (Y)es with defaults, yes with (C)ustomization, or (N)o. [Y]:
Enter name for sample changelog file to be created or (s)kip [example-changelog]:
Enter name for defaults file to be created or (s)kip [liquibase.properties]:
Enter the JDBC url without username or password to be used (What is a JDBC url? <url>) [jdbc:h2:tcp://localhost:9090/mem:dev]:
Enter username to connect to JDBC url [dbuser]:
Enter password to connect to JDBC url [letmein]:
Setting up new Liquibase project in '""",projectDirectory,
                "Created example changelog file '",
                directory,"hello.changelog.sql'",
                "Created example defaults file '",
                directory,"banana.properties'",
                "To use the new project files, please cd into '",projectDirectory,"', make sure your database is active and accessible and run \"liquibase update --defaults-file=banana.properties\". For more details, visit the Getting Started Guide at https://docs.liquibase.com/start/home.html"
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: Paths.get(projectDirectory, "hello.changelog.sql").toString().replace('\\', '/'),
                usedH2: false,
                projectDefaultsFile: Paths.get(projectDirectory, defaultsFile)
        ]
    }


    run "Interactive with changelogfile argument", {
        String defaultsFile = "banana.properties"
        setup {
            createTempResource("changelogs/init/example-changelog.xml", "examples/xml/example-changelog.xml", "target")
            createTempResource("liquibase.yaml.properties", "examples/sql/liquibase.properties", "target")
            cleanResources("target/examples/xml/example-changelog.xml", "target/examples/sql/liquibase.properties", "newdir")
        }
        arguments = [
                changelogFile: "thistest.sql"
        ]
        testUI = new CommandTests.TestUIWithAnswers(["c","newdir",  defaultsFile, "jdbcurl", "username", "password"] as String[])
        expectedUI = [
                """Setup new liquibase.properties, flowfile, and sample changelog? Enter (Y)es with defaults, yes with (C)ustomization, or (N)o. [Y]:
Enter a relative path to desired project directory [./]: 
Enter name for defaults file to be created or (s)kip [liquibase.properties]: 
Enter the JDBC url without username or password to be used (What is a JDBC url? <url>) [jdbc:h2:tcp://localhost:9090/mem:dev]: 
Enter username to connect to JDBC url [dbuser]: 
Enter password to connect to JDBC url [letmein]: 
Setting up new Liquibase project in '""","newdir'...",
                "Created example changelog file '",
                "newdir","thistest.sql'",
                "Created example defaults file '",
                "newdir","banana.properties'",
                "To use the new project files, please cd into 'newdir', make sure your database is active and accessible and run \"liquibase update --defaults-file=banana.properties\". For more details, visit the Getting Started Guide at https://docs.liquibase.com/start/home.html"
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: Paths.get("newdir", "thistest.sql").toString().replace('\\', '/'),
                usedH2: false,
                projectDefaultsFile: Paths.get("newdir", defaultsFile)
        ]
    }

    run "Interactive with format argument", {
        String defaultsFile = "banana.properties"
        setup {
            createTempResource("changelogs/init/example-changelog.xml", "examples/xml/example-changelog.xml", "target")
            createTempResource("liquibase.yaml.properties", "examples/sql/liquibase.properties", "target")
            cleanResources("target/examples/xml/example-changelog.xml", "target/examples/sql/liquibase.properties", "newdir")
        }
        arguments = [
                format: "yml"
        ]
        testUI = new CommandTests.TestUIWithAnswers(["c","newdir", "hello.changelog",  defaultsFile, "jdbcurl", "username", "password"] as String[])
        expectedUI = [
                """Setup new liquibase.properties, flowfile, and sample changelog? Enter (Y)es with defaults, yes with (C)ustomization, or (N)o. [Y]:
Enter a relative path to desired project directory [./]: 
Enter name for sample changelog file to be created or (s)kip [example-changelog]: 
Enter name for defaults file to be created or (s)kip [liquibase.properties]: 
Enter the JDBC url without username or password to be used (What is a JDBC url? <url>) [jdbc:h2:tcp://localhost:9090/mem:dev]: 
Enter username to connect to JDBC url [dbuser]: 
Enter password to connect to JDBC url [letmein]: 
Setting up new Liquibase project in ""","newdir'...",
                "Created example changelog file '",
                "newdir","hello.changelog.yml'",
                "Created example defaults file '",
                "newdir","banana.properties'",
                "To use the new project files, please cd into 'newdir', make sure your database is active and accessible and run \"liquibase update --defaults-file=banana.properties\". For more details, visit the Getting Started Guide at https://docs.liquibase.com/start/home.html"
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: Paths.get("newdir", "hello.changelog.yml").toString().replace('\\', '/'),
                usedH2: false,
                projectDefaultsFile: Paths.get("newdir", defaultsFile)
        ]
        expectedFileContent = [
                "./newdir/banana.properties" : [CommandTests.assertContains("hello.changelog.yml")]
        ]
        expectFileToExist = new File("./newdir", "hello.changelog.yml")
    }

    run "Interactive with defaultsfile argument", {
        String defaultsFile = "newdefaultsfile.properties"
        setup {
            createTempResource("changelogs/init/example-changelog.xml", "examples/xml/example-changelog.xml", "target")
            createTempResource("liquibase.yaml.properties", "examples/sql/liquibase.properties", "target")
            cleanResources("target/examples/xml/example-changelog.xml", "target/examples/sql/liquibase.properties", "newdir")
        }
        arguments = [
                projectDefaultsFile: defaultsFile
        ]
        testUI = new CommandTests.TestUIWithAnswers(["c","newdir", "hello.changelog.sql", "jdbcurl", "username", "password"] as String[])
        expectedUI = [
                """Setup new liquibase.properties, flowfile, and sample changelog? Enter (Y)es with defaults, yes with (C)ustomization, or (N)o. [Y]:
Enter a relative path to desired project directory [./]: 
Enter name for sample changelog file to be created or (s)kip [example-changelog]: 
Enter the JDBC url without username or password to be used (What is a JDBC url? <url>) [jdbc:h2:tcp://localhost:9090/mem:dev]: 
Enter username to connect to JDBC url [dbuser]: 
Enter password to connect to JDBC url [letmein]: 
Setting up new Liquibase project in '""","newdir'...",
                "Created example changelog file '",
                "newdir","hello.changelog.sql'",
                "Created example defaults file '",
                "newdir","newdefaultsfile.properties'",
                "To use the new project files, please cd into 'newdir', make sure your database is active and accessible and run \"liquibase update --defaults-file=newdefaultsfile.properties\". For more details, visit the Getting Started Guide at https://docs.liquibase.com/start/home.html"
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: Paths.get("newdir", "hello.changelog.sql").toString().replace('\\', '/'),
                usedH2: false,
                projectDefaultsFile: Paths.get("newdir", defaultsFile)
        ]
    }

    run "Interactive with username argument", {
        String defaultsFile = "banana.properties"
        setup {
            createTempResource("changelogs/init/example-changelog.xml", "examples/xml/example-changelog.xml", "target")
            createTempResource("liquibase.yaml.properties", "examples/sql/liquibase.properties", "target")
            cleanResources("target/examples/xml/example-changelog.xml", "target/examples/sql/liquibase.properties", "newdir")
        }
        arguments = [
                username: "disusername"
        ]
        testUI = new CommandTests.TestUIWithAnswers(["c","newdir", "hello.changelog.sql",  defaultsFile, "jdbcurl", "password"] as String[])
        expectedUI = [
                """Setup new liquibase.properties, flowfile, and sample changelog? Enter (Y)es with defaults, yes with (C)ustomization, or (N)o. [Y]:
Enter a relative path to desired project directory [./]: 
Enter name for sample changelog file to be created or (s)kip [example-changelog]: 
Enter name for defaults file to be created or (s)kip [liquibase.properties]: 
Enter the JDBC url without username or password to be used (What is a JDBC url? <url>) [jdbc:h2:tcp://localhost:9090/mem:dev]: 
Enter password to connect to JDBC url [letmein]: 
Setting up new Liquibase project in '""","newdir'...",
                "Created example changelog file '",
                "newdir","hello.changelog.sql'",
                "Created example defaults file '",
                "newdir","banana.properties'",
                "To use the new project files, please cd into 'newdir', make sure your database is active and accessible and run \"liquibase update --defaults-file=banana.properties\". For more details, visit the Getting Started Guide at https://docs.liquibase.com/start/home.html"
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: Paths.get("newdir", "hello.changelog.sql").toString().replace('\\', '/'),
                usedH2: false,
                projectDefaultsFile: Paths.get("newdir", defaultsFile)
        ]
    }

    run "Interactive with url argument", {
        String defaultsFile = "banana.properties"
        setup {
            createTempResource("changelogs/init/example-changelog.xml", "examples/xml/example-changelog.xml", "target")
            createTempResource("liquibase.yaml.properties", "examples/sql/liquibase.properties", "target")
            cleanResources("target/examples/xml/example-changelog.xml", "target/examples/sql/liquibase.properties", "newdir")
        }
        arguments = [
                url: "myurl"
        ]
        testUI = new CommandTests.TestUIWithAnswers(["c","newdir", "hello.changelog.sql",  defaultsFile, "username", "password\\backslash"] as String[])
        expectedUI = [
                """Setup new liquibase.properties, flowfile, and sample changelog? Enter (Y)es with defaults, yes with (C)ustomization, or (N)o. [Y]:
Enter a relative path to desired project directory [./]: 
Enter name for sample changelog file to be created or (s)kip [example-changelog]: 
Enter name for defaults file to be created or (s)kip [liquibase.properties]: 
Enter username to connect to JDBC url [dbuser]: 
Enter password to connect to JDBC url [letmein]: 
Setting up new Liquibase project in '""","newdir'...",
                "Created example changelog file '",
                "newdir","hello.changelog.sql'",
                "Created example defaults file '",
                "newdir","banana.properties'",
                "To use the new project files, please cd into 'newdir', make sure your database is active and accessible and run \"liquibase update --defaults-file=banana.properties\". For more details, visit the Getting Started Guide at https://docs.liquibase.com/start/home.html"
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: Paths.get("newdir", "hello.changelog.sql").toString().replace('\\', '/'),
                usedH2: false,
                projectDefaultsFile: Paths.get("newdir", defaultsFile)
        ]
        expectedFileContent = [
                "./newdir/banana.properties" : [CommandTests.assertContains("password\\backslash")]
        ]
    }

    run "Interactive with password argument", {
        String defaultsFile = "banana.properties"
        setup {
            createTempResource("changelogs/init/example-changelog.xml", "examples/xml/example-changelog.xml", "target")
            createTempResource("liquibase.yaml.properties", "examples/sql/liquibase.properties", "target")
            cleanResources("target/examples/xml/example-changelog.xml", "target/examples/sql/liquibase.properties", "newdir")
        }
        arguments = [
                password: "helloworld"
        ]
        testUI = new CommandTests.TestUIWithAnswers(["c","newdir", "hello.changelog.sql",  defaultsFile, "jdbcurl", "username"] as String[])
        expectedUI = [
                """Setup new liquibase.properties, flowfile, and sample changelog? Enter (Y)es with defaults, yes with (C)ustomization, or (N)o. [Y]:
Enter a relative path to desired project directory [./]: 
Enter name for sample changelog file to be created or (s)kip [example-changelog]: 
Enter name for defaults file to be created or (s)kip [liquibase.properties]: 
Enter the JDBC url without username or password to be used (What is a JDBC url? <url>) [jdbc:h2:tcp://localhost:9090/mem:dev]: 
Enter username to connect to JDBC url [dbuser]: 
Setting up new Liquibase project in '""","newdir'...",
                "Created example changelog file '",
                "newdir","hello.changelog.sql'",
                "Created example defaults file '",
                "newdir","banana.properties'",
                "To use the new project files, please cd into 'newdir', make sure your database is active and accessible and run \"liquibase update --defaults-file=banana.properties\". For more details, visit the Getting Started Guide at https://docs.liquibase.com/start/home.html"
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: Paths.get("newdir", "hello.changelog.sql").toString().replace('\\', '/'),
                usedH2: false,
                projectDefaultsFile: Paths.get("newdir", defaultsFile)
        ]
    }

    run "Interactive and changelog file exists in the project directory", {
        String defaultsFile = "banana.properties"
        String projectDirectory = "target/test-classes/projectDirectory"+ StringUtil.randomIdentifer(10)
        setup {
            createTempDirectoryResource(projectDirectory)
            createTempResource("changelogs/init/example-changelog.sql", "my-existing-changelog.sql", projectDirectory)
            createTempResource("liquibase.yaml.properties", "examples/sql/liquibase.properties", "target")
            cleanResources("target/examples/sql/liquibase.properties", projectDirectory)
        }
        expectedResults = [
                statusCode   : 0,
                changelogFile: Paths.get(projectDirectory, "my-existing-changelog.sql").toString().replace('\\', '/'),
                usedH2: false,
                projectDefaultsFile: Paths.get(projectDirectory, defaultsFile)
        ]
        expectFileToExist = new File(projectDirectory,"liquibase.properties")
        expectFileToExist = new File(projectDirectory,"my-existing-changelog.sql")
        testUI = new CommandTests.TestUIWithAnswers(["c",projectDirectory, "",  defaultsFile, "jdbcurl", "username", "password"] as String[])
        expectedUI = [
                """Setup new liquibase.properties, flowfile, and sample changelog? Enter (Y)es with defaults, yes with (C)ustomization, or (N)o. [Y]:
Enter a relative path to desired project directory [./]: 
Enter name for sample changelog file to be created or (s)kip [my-existing-changelog.sql]: 
Enter name for defaults file to be created or (s)kip [liquibase.properties]: 
Enter the JDBC url without username or password to be used (What is a JDBC url? <url>) [jdbc:h2:tcp://localhost:9090/mem:dev]: 
Enter username to connect to JDBC url [dbuser]: 
Enter password to connect to JDBC url [letmein]:"""
        ]
        expectedFileContent = [
                (projectDirectory + "/banana.properties") : [
                        CommandTests.assertContains("changeLogFile=my-existing-changelog.sql"),
                        CommandTests.assertNotContains("#changeLogFile=")
                ]
        ]
    }

    run "Interactive and multiple changelog files exist in the project directory", {
        String defaultsFile = "banana.properties"
        String projectDirectory = "target/test-classes/projectDirectory"+ StringUtil.randomIdentifer(10)
        setup {
            createTempDirectoryResource(projectDirectory)
            createTempResource("changelogs/init/example-changelog.sql", "my-existing-changelog.sql", projectDirectory, new Date(120, 02, 10))
            createTempResource("changelogs/init/example-changelog.sql", "my-other-newer-existing-changelog.sql", projectDirectory)
            createTempResource("liquibase.yaml.properties", "examples/sql/liquibase.properties", "target")
            cleanResources("examples/sql/liquibase.properties", projectDirectory)
        }
        expectedResults = [
                statusCode   : 0,
                changelogFile: Paths.get(projectDirectory, "my-other-newer-existing-changelog.sql").toString().replace('\\', '/'),
                usedH2: false,
                projectDefaultsFile: Paths.get(projectDirectory, defaultsFile)
        ]
        expectFileToExist = new File(projectDirectory,"liquibase.properties")
        expectFileToExist = new File(projectDirectory,"my-existing-changelog.sql")
        testUI = new CommandTests.TestUIWithAnswers(["c",projectDirectory, "",  defaultsFile, "jdbcurl", "username", "password"] as String[])
        expectedUI = [
                """Setup new liquibase.properties, flowfile, and sample changelog? Enter (Y)es with defaults, yes with (C)ustomization, or (N)o. [Y]:
Enter a relative path to desired project directory [./]: 
Enter name for sample changelog file to be created or (s)kip [my-other-newer-existing-changelog.sql]: 
Enter name for defaults file to be created or (s)kip [liquibase.properties]: 
Enter the JDBC url without username or password to be used (What is a JDBC url? <url>) [jdbc:h2:tcp://localhost:9090/mem:dev]: 
Enter username to connect to JDBC url [dbuser]: 
Enter password to connect to JDBC url [letmein]:"""
        ]
    }

    run "Interactive skipping changelog file", {
        setup {
            createTempResource("changelogs/init/example-changelog.xml", "examples/xml/example-changelog.xml", "target")
            createTempResource("liquibase.yaml.properties", "examples/sql/liquibase.properties", "target")
            cleanResources("examples/xml/example-changelog.xml", "examples/sql/liquibase.properties", "dir")
        }
        testUI = new CommandTests.TestUIWithAnswers(["c", "dir", "s", "", "", "" ,""] as String[])
        expectedUI = [
"""
Setup new liquibase.properties, flowfile, and sample changelog? Enter (Y)es with defaults, yes with (C)ustomization, or (N)o. [Y]: 
Enter a relative path to desired project directory [./]:
Enter name for sample changelog file to be created or (s)kip [example-changelog]:
No changelog file will be created. Specify a valid changelog file on the CLI, via Environment variable, or in your defaults file. 
For more details, visit the Getting Started Guide at https://docs.liquibase.com/start/home.html
Enter name for defaults file to be created or (s)kip [liquibase.properties]: 
Enter the JDBC url without username or password to be used (What is a JDBC url? <url>) [jdbc:h2:tcp://localhost:9090/mem:dev]: 
Enter username to connect to JDBC url [dbuser]: 
Enter password to connect to JDBC url [letmein]: 
Setting up new Liquibase project in '
"""
        ]
        expectedResults = [
            statusCode   : 0,
            changelogFile: Paths.get("dir", "example-changelog.sql").toString().replace('\\', '/'),
            usedH2: true,
            projectDefaultsFile: Paths.get("dir", "liquibase.properties")
        ]
        expectedFileContent = [
                "./dir/liquibase.properties" : [CommandTests.assertContains("#changeLogFile=")]
        ]
        expectFileToNotExist = new File("dir", "example-changelog.sql")
    }

    run "Interactive skipping defaults file", {
        setup {
            createTempResource("changelogs/init/example-changelog.xml", "examples/xml/example-changelog.xml", "target")
            createTempResource("liquibase.yaml.properties", "examples/sql/liquibase.properties", "target")
            cleanResources("examples/xml/example-changelog.xml", "examples/sql/liquibase.properties", "dir")
        }
        testUI = new CommandTests.TestUIWithAnswers(["c", "dir", "", "", "s"] as String[])
        expectedUI = ["""Setup new liquibase.properties, flowfile, and sample changelog? Enter (Y)es with defaults, yes with (C)ustomization, or (N)o. [Y]: 
Enter a relative path to desired project directory [./]: 
Enter name for sample changelog file to be created or (s)kip [example-changelog]: 
Enter your preferred changelog format (options: sql, xml, json, yml, yaml) [sql]: 
Enter name for defaults file to be created or (s)kip [liquibase.properties]: 
No defaults file will be created. Specify a valid defaults file on the CLI, via Environment variable, or pass all required properties on the CLI, via Environment variables.
For more details, visit the Getting Started Guide at https://docs.liquibase.com/start/home.html
Setting up new Liquibase project in '""", "dir'..."
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: Paths.get("dir", "example-changelog.sql").toString().replace('\\', '/'),
                usedH2: false,
                projectDefaultsFile: Paths.get("dir", "liquibase.properties")
        ]
        expectFileToExist = new File("dir", "example-changelog.sql")
        expectFileToNotExist = new File("dir", "liquibase.properties")
    }

    run "Interactive skipping both files", {
        setup {
            createTempResource("changelogs/init/example-changelog.xml", "examples/xml/example-changelog.xml", "target")
            createTempResource("liquibase.yaml.properties", "examples/sql/liquibase.properties", "target")
            cleanResources("examples/xml/example-changelog.xml", "examples/sql/liquibase.properties", "dir")
        }
        testUI = new CommandTests.TestUIWithAnswers(["c", "dir", "s", "s"] as String[])
        expectedUI = [
"""
Setup new liquibase.properties, flowfile, and sample changelog? Enter (Y)es with defaults, yes with (C)ustomization, or (N)o. [Y]: 
Enter a relative path to desired project directory [./]: 
Enter name for sample changelog file to be created or (s)kip [example-changelog]:
No changelog file will be created. Specify a valid changelog file on the CLI, via Environment variable, or in your defaults file.
For more details, visit the Getting Started Guide at https://docs.liquibase.com/start/home.html
Enter name for defaults file to be created or (s)kip [liquibase.properties]: 
No defaults file will be created. Specify a valid defaults file on the CLI, via Environment variable, or pass all required properties on the CLI, via Environment variables.
For more details, visit the Getting Started Guide at https://docs.liquibase.com/start/home.html
"""
        ]
        expectedResults = [
                statusCode   : 0,
                usedH2: false
        ]
        expectFileToNotExist = new File("dir", "example-changelog.sql")
        expectFileToNotExist = new File("dir", "liquibase.properties")
        expectFileToExist = new File("dir")
    }

    run "Yes with defaults should create all example flow files", {
        String projectDirectory = "target/test-classes/projectDirectory"
        String defaultsFile = InitProjectCommandStep.INIT_DEFAULTS_FILE_ARG.getDefaultValue()
        setup {
            createTempDirectoryResource(projectDirectory)
            createTempResource("changelogs/init/example-changelog.sql", "my-changelog.sql", "target/test-classes/projectDirectory")
            createTempResource("liquibase.yaml.properties", "examples/sql/liquibase.properties", "target")
            cleanResources("target/examples/sql/liquibase.properties", projectDirectory)
        }
        arguments = [
                projectDir: projectDirectory,
                changelogFile: "my-changelog.sql",
                copyExampleFlowFiles: "true",
                copyExampleChecksPackageFile: "true",
        ]
        testUI = new CommandTests.TestUIWithAnswers(["y"] as String[])
        expectedUI = [
                "Setup new liquibase.properties, flowfile, and sample changelog? Enter (Y)es with defaults, yes with (C)ustomization, or (N)o. [Y]:",
                "liquibase.flowvariables.yaml",
                "liquibase.flowfile.yaml",
                "liquibase.advanced.flowfile.yaml",
                "liquibase.endstage.flow",
                "liquibase.checks-package.yaml",
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: Paths.get(projectDirectory, "my-changelog.sql").toString().replace('\\', '/'),
                usedH2: true,
                projectDefaultsFile: Paths.get(projectDirectory, defaultsFile)
        ]
        expectedFileContent = [
                (projectDirectory + "/liquibase.flowvariables.yaml") : [CommandTests.assertContains("This example yaml file of key:value variables")],
                (projectDirectory + "/liquibase.flowfile.yaml") : [CommandTests.assertContains("Any command which fails in any stage below result in the command stopping")],
                (projectDirectory + "/liquibase.advanced.flowfile.yaml") : [CommandTests.assertContains("Advanced options show in this file include")],
                (projectDirectory + "/liquibase.endstage.flow") : [CommandTests.assertContains("The endStage ALWAYS RUNS")],
        ]
    }

    run "Interactive should create example flow files", {
        String defaultsFile = "banana.properties"
        String projectDirectory = "newdir"
        setup {
            createTempResource("changelogs/init/example-changelog.xml", "examples/xml/example-changelog.xml", "target")
            createTempResource("liquibase.yaml.properties", "examples/sql/liquibase.properties", "target")
            cleanResources("target/examples/xml/example-changelog.xml", "target/examples/sql/liquibase.properties", "newdir")
        }
        arguments = [
                format: "yml",
                copyExampleFlowFiles: "true",
                copyExampleChecksPackageFile: "true",
        ]
        testUI = new CommandTests.TestUIWithAnswers(["c", projectDirectory, "hello.changelog",  defaultsFile, "jdbcurl", "username", "password"] as String[])
        expectedUI = [
                """Setup new liquibase.properties, flowfile, and sample changelog? Enter (Y)es with defaults, yes with (C)ustomization, or (N)o. [Y]:
Enter a relative path to desired project directory [./]: 
Enter name for sample changelog file to be created or (s)kip [example-changelog]: 
Enter name for defaults file to be created or (s)kip [liquibase.properties]: 
Enter the JDBC url without username or password to be used (What is a JDBC url? <url>) [jdbc:h2:tcp://localhost:9090/mem:dev]: 
Enter username to connect to JDBC url [dbuser]: 
Enter password to connect to JDBC url [letmein]: 
Setting up new Liquibase project in """,
                "liquibase.flowvariables.yaml",
                "liquibase.flowfile.yaml",
                "liquibase.advanced.flowfile.yaml",
                "liquibase.endstage.flow",
                "liquibase.checks-package.yaml",
        ]
        expectedResults = [
                statusCode   : 0,
                changelogFile: Paths.get(projectDirectory, "hello.changelog.yml").toString().replace('\\', '/'),
                usedH2: false,
                projectDefaultsFile: Paths.get(projectDirectory, defaultsFile)
        ]
        expectedFileContent = [
                (projectDirectory + "/liquibase.flowvariables.yaml") : [CommandTests.assertContains("This example yaml file of key:value variables")],
                (projectDirectory + "/liquibase.flowfile.yaml") : [CommandTests.assertContains("Any command which fails in any stage below result in the command stopping")],
                (projectDirectory + "/liquibase.advanced.flowfile.yaml") : [CommandTests.assertContains("Advanced options show in this file include")],
                (projectDirectory + "/liquibase.endstage.flow") : [CommandTests.assertContains("The endStage ALWAYS RUNS")],
        ]
    }
}
