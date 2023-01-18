# Developing Liquibase

## Overview

Liquibase is implemented in Java, and uses Maven as its build system. 

The code structure follows the standard Maven directory structure divided into modules, with `liquibase-core` being the most central module.

## Testing

While there are still older test classes written in JUnit, tests should be written in (or if possible migrated to) [Spock](https://spockframework.org/).

### Integration Testing

**NOTE: THIS DESCRIBES AN ALPHA SYSTEM WHICH IS LIKELY TO CHANGE. CHECK BACK REGULARLY**

Because Liquibase interacts with many different databases, it is important that we automate the testing of those interactions.

### Integration Testing Environments

In order to write a test against an external database, you must first have a database to test against. 
Liquibase handles this for you both to keep you focused on test writing, and also to provide standardization and repeatability to the tests.

#### Referencing a test system from your tests  

Within your tests, you create a reference to the system you are going to test against by calling `testSystem = Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("h2")` where the argument to `getTestSystem()` is the key for a configured system.
The default  test systems are configured in [liquibase.sdk.yaml](liquibase-extension-testing/src/main/resources/liquibase.sdk.yaml). 
When that line in your test is hit, the TestSystemFactory will use a running system if available, or start a new system if needed. 
Any newly-started systems will be available for the lifetime of **all** the tests to be shared between them and be shut down at the end of the entire run.

The base TestSystem class is designed to work for more than just Databases, so if you know you are connecting to a database you will want to cast it to a DatabaseTestSystem in order to call methods like `getConnection()` for use in connecting to it. 
For example:
```java
@Shared
private DatabaseTestSystem mysql = 
    (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("mysql")
```

When TestSystems need to be started, Liquibase generally relies on Docker instances. When docker images are not available or are unneeded, they can also connect to an external database via a `url` setting. 

When using docker instances, if we want to add support for a new database we want to test against, we need to create a new test class extended from `DatabaseTestSystem` and override
`createContainerWrapper()` method, as displayed below:
```java
public class FirebirdTestSystem  extends DatabaseTestSystem {

    public FirebirdTestSystem(Definition definition) {
        super(definition);
    }

    @Override
    protected DatabaseWrapper createContainerWrapper() throws Exception {      
        return new DockerDatabaseWrapper(new FirebirdContainer(
                DockerImageName.parse(getImageName()).withTag(getVersion()))
                .withDatabaseName(getCatalog())
                .withUsername(getUsername())
                .withPassword(getPassword()),
                this
        );
    }
}
```

Additionally, we need to add the test dependency for the DB we are adding support for in the `pom.xml` file from **liquibase-extension-testing** module:
```xml
<dependency>
    <groupId>org.firebirdsql</groupId>
    <artifactId>firebird-testcontainers-java</artifactId>
    <version>1.2.0</version>
</dependency>
```

#### Controlling which systems are tested against

The test framework is designed to automatically launch database instances of any type. However, database tests can be slow, so Liquibase defaults to testing in-memory databases (h2, hsqldb, sqlite). Any tests requiring other database platforms will be automatically marked as "ignored".

The `liquibase.sdk.testSystem.test` attribute to be a comma-separated list specified in the `liquibase.sdk.local.yml` file (see below).

The `liquibase.sdk.testSystem.test` setting can also specify specific settings for the test systems you wish to test against. 
For example, if you have `liquibase.sdk.testSystem.test=h2?version=1.4.200,mysql`, when your tests call `getTestSystem("h2")` it will get an h2 version 1.4.200 system. You can also specify profiles (see below) as well, like `liquibase.sdk.testSystem.test=mssql:case-sensitive,mysql`    

#### Starting test systems independently from tests

Often times you need to have a database you can manually run liquibase against outside of your automated tests. 
The `liquibase-extension-testing.jar` and corresponding `liquibase-extension-testing-deps.jar` files add a `liquibase sdk system` command group to the Liquibase CLI which allow you to start and stop the same containers used by your test.
By adding those jars to your LIQUIBASE_HOME/lib directory, you can run `liquibase sdk system up --name mysql` to bring up a mysql system, where the `name` argument is the name of the test system you want to start.

To shut down the instances started this way, run `liquibase sdk system down --name mysql`

#### Local configuration

The `liquibase.sdk.yaml` file contains the default configuration, but there are times you may want to change the default behavior. 
By creating a `liquibase.sdk.local.yml` file in the [same directory](liquibase-extension-testing/src/main/resources) as the `liquibase.sdk.yaml` file, you can add just the settings you want to override.

NOTE: The `liquibase.sdk.local.yml` is specific to your development environment and gitignore is configured to disallow the file from being committed.

Below there is an example of a default configuration which is going to be used across the different profiles, unless any of them overrides any given property:
```yaml
liquibase:
  sdk:
    testSystem:
      default:
          username: lbuser
          password: LiquibasePass1
          catalog: lbcat
          altCatalog: lbcat2
          altSchema: lbschem2
          altTablespace: liquibase2
          keepRunning: true         
      test: mysql,h2,mssql     
      acceptLicenses: mssql
```
will run tests against mysql, h2, and mssql. It also specifies that you accept the EULA license the mssql docker container requires you to accept.

##### Test System Profiles

In both the standard `liquibase.sdk.yaml` and `liquibase.sdk.local.yaml` files, "profiles" can be defined which override specific settings from the default configuration.
For example, the h2 configuration defines a "1.x" and a "2.x" profile:

```yaml
  h2:
    url: jdbc:h2:mem:${catalog};DB_CLOSE_DELAY=-1
    profiles:
      "1.x":
        version: 1.4.200
      "2.x":
        version: 2.0.206
```

The advantage of profiles is to allow users to specify the exact version of h2 relevant for their testing.
