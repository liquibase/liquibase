# A Harness of Integration Tests
## Framework
The Harness Test framework logically consists of 2 main folders:
1) A liquibase-integration-test/src/test/groovy/liquibase/harness with groovy code and
2) A liquibase-integration-test/src/test/resources/harness with test resources

## Test Scope
#### Change Object Test
* At present there is only one test class to execute, which is `groovy/liquibase/harness/MainTestIT.groovy` -- This test class
will execute a set of test cases based on provided input. 
* The test input is configured via `resources/harness/testConfig.yml` -- This yaml file takes 
input changelogs from the `resources/harness/changelogs` folder 
* The test behavior is as follows:
  * It reads in the changesets from the changelogs provided 
  * Runs the changeset thru the SqlGeneratorFactory to generate SQL
  * Compares the generated SQL with the expected SQL (provided in `resources/harness/expectedSql`)
  * If the SQL generation is correct, the test then runs `liquibase update` to deploy the
  changeset to the DB
  * The test takes a snapshot of the database after deployment
  * Finally, the actual DB snapshot is compared to the expected DB snapshot (provided in `resources/harness/expectedSnapshot`)

#### Adding a change object test
1) Go to `src/test/resources/harness/changelogs` and add the xml changeset for the change type you want to test.
2) Go to `src/test/resources/harness/expectedSQL` and add the expected generated SQL. 
You will need to add this under the database specific folder. Currently we only have Postgresql & MySQL folders. 
If you would like to test another DB type, please add the requisite folder.
3) Go to `src/test/resources/harness/expectedSnapshot` and add the expected DB Snapshot results. 
You will need to add this under the database specific folder. Currently we only have Postgresql & MySQL folders. 
If you would like to test another DB type, please add the requisite folder.
4) Go to your IDE and run the test class `MainTestIT.groovy`

## Running the integration test suite is as easy as one-two-three
1) Make sure you have docker container up and running first
2) Go to  `liquibase-integration-tests/src/test/resources/harness/docker` and run `docker-compose up -d`. 
Wait until the databases start up.
3) Open `liquibase-integration-tests/src/test/groovy/liquibase/harness/CreateTable.groovy` in your IDE of choice 
and run the test class `MainTestIT.groovy`

## Cleanup
When you are done with test execution, run `docker-compose down --volumes` to stop the docker containers 
gracefully and to allow the tests to start from a clean slate on the next run.

P.S. - Please note that the Maven surefire plugin isn't configured to run the harness tests yet. This is in the works.



#### Stay tuned, there is more to come!