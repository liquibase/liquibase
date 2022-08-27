# Liquibase Support for Google BigQuery

Liquibase extension for BigQuery.

# Using the Liquibase Test Harness in BigQuery Extension

### Configuring the project

Fill in `harness-config.yml` in `src/test/resources` directory. 

### Executing with maven

To run all harness tests for the extension run

```
mvn -Dit.test=LiquibaseHarnessSuiteIT verify
```

To run only subset of tests, add `-DchangeObjects` parameter, with list of tests that should be included

```
mvn -Dit.test=LiquibaseHarnessSuiteIT -DchangeObjects=addLookupTable verify
 ```

### Executing from IDE

From IDE you can run `src/test/groovy/LiquibaseHarnessSuiteIT` test. 
You can narrow down the list of harness tests by specifying `-DchangeObjects` VM argument in Run Configuration.


