# Harness 
Harness integration tests framework logically consist of 2 main folders liquibase-integration-test/src/test/groovy/liquibase/harness with groovy code
and liquibase-integration-test/src/test/resources/harness with test resources

For now there is only one test class `groovy/liquibase/harness/MainTestIT.groovy` 
that runs number of testcases based on provided input that is configured by `resources/harness/testConfig.yml`
It takes input change logs from `resources/harness/changelogs` folder and compare generated SQL queries and DB snapshots from ones provided in expected files 
(`resources/harness/expectedSnapshot` and `resources/harness/expectedSql`) according to DB type

## Running integration test suite is as easy as one-two-three
1) make sure you have docker container up and running
2) go to  `liquibase-integration-tests/src/test/resources/harness/docker` and run `docker-compose up`, wait until it starts
3) open `liquibase-integration-tests/src/test/groovy/liquibase/harness/CreateTable.groovy` in IDE and run test itself
PS. surefire plugin isn't configured to run harness test yet

Stay tuned, there is more to come