# Liquibase Support for Google BigQuery

A Liquibase extension adding support for Google BigQuery.
Include this in your application project to run Liquibase database migration scripts
against a Google BigQuery database.

Try out the [Liquibase + Bigquery with the getting started tutorial](https://contribute.liquibase.com/extensions-integrations/directory/database-tutorials/bigquery/).

## Supported features

The following Liquibase ChangeTypes are supported:

- createTable
- dropTable
- addColumn
- modifyDataType
- addLookupTable
- createView
- dropView

## Limitations

Currently, not supported are:

- constraints
- sequences
- primary and foreign keys
- remarks
- column merging
- auto-increments
- check constraints
- indexes
- triggers

## Getting started

### Installing and setting up liquibase

Download and install liquibase from
[here](https://docs.liquibase.com/install/liquibase-windows.html).

### JDBC driver for BigQuery

You will also need JDBC driver for BigQuery.
BigQuery documentation will redirect you to where you can
[download](https://cloud.google.com/bigquery/docs/reference/odbc-jdbc-drivers)
supported JDBC/ODBC drivers

Click JDBC 4.2-compatible to start downloading the corresponding zip file.
When you unzip it you will find quite some number of jars and one of them,
named GoogleBigQueryJDBC42.jar,
will represent our BigQuery JDBC driver.
Add files from the archive to the liquibase lib directory.

### Liquibase BigQuery extension

Build this project with `mvn` or download the jar file
corresponding to the latest release and put it
into `liquibase/lib` lib directory.

### Starting BigQuery dataset

You can create a BigQuery dataset in the GCP console, use terraform or bq client.

### Test your connection

Give the extension temporary use of your BigQuery user credentials for API access
by running the following gcloud command:

```sh
gcloud auth application-default login
```

### Create configuration file

Configure the connection in the file `liquibase.properties`

```properties
driver:
url:
logLevel:
```

url represents JDBC connection string.
After the official documentation the following is the format of the
connection URL for the Simba Google BigQuery JDBC Connector:

`jdbc:bigquery://[Host]:[Port];ProjectId=[Project];OAuthType= [AuthValue];
[Property1]=[Value1];[Property2]=[Value2];…`

where:

- [Host] - is the DNS or IP address of the server.
Set it to `https://www.googleapis.com/bigquery/v2`
- [Port] - is the number of the TCP port to connect to. Set it to 443.
- [Project] - is the id of your BigQuery project.
- [AuthValue] - is a number that specifies the type of authentication
used by the connector.

The Simba Google BigQuery JDBC Connector uses the OAuth 2.0 protocol
for authentication and authorization.
It authenticates your connection through Google OAuth APIs.
You can configure the connector to provide your credentials
and authenticate the connection to the database using one of the
following methods:

- Using a Google User Account
- Using a Google Service Account
- Using Pre-Generated Access and Refresh Tokens
- Using Application Default Credentials

Example shows authentication using Google Service Account

```properties
driver: com.simba.googlebigquery.jdbc.Driver
url: jdbc:bigquery://https://www.googleapis.com/bigquery/v2:443;ProjectId=<PROJECT_ID>;\
  DefaultDataset=<DATASET_ID>;OAuthType=0;OAuthServiceAcctEmail=<SA_ACCOUNT>;\
  OAuthPvtKeyPath=<PATH_TO_KEY_FOR_SA>;
logLevel: WARN
```

### Run

- updating the database schema

```shell
liquibase --changeLogFile=changelog.bigquery.sql update
```

- generating the schema from current database state

More detailed instructions for getting started can be found
[here](https://medium.com/google-cloud/version-control-of-bigquery-schema-changes-with-liquibase-ddc7092d6d1d)

## Using the Liquibase Test Harness in BigQuery Extension

### Configuring the project

Fill in `harness-config.yml` in `src/test/resources` directory.

### Executing with maven

Build project without tests

```sh
mvn package -DskipTests
```

To run all harness tests for the extension run

```sh
mvn -Dit.test=LiquibaseHarnessSuiteIT verify
```

To run only subset of tests, add `-DchangeObjects` parameter,
with list of tests that should be included

```sh
mvn -Dit.test=LiquibaseHarnessSuiteIT -DchangeObjects=addLookupTable verify
 ```

### Executing from IDE

From IDE you can run `src/test/groovy/LiquibaseHarnessSuiteIT` test.
You can narrow down the list of harness tests by specifying `-DchangeObjects`
VM argument in Run Configuration.
