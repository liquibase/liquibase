# Liquibase [![Build and Test](https://github.com/liquibase/liquibase/actions/workflows/build.yml/badge.svg)](https://github.com/liquibase/liquibase/actions/workflows/build.yml)
<p align="center"><img src="https://github.com/liquibase/liquibase/blob/master/Liquibase.png" width="30%" height="30%"></p>

Liquibase helps millions of teams track, version, and deploy database schema changes. It will help you to:
- Control database schema changes for specific versions
- Eliminate errors and delays when releasing databases
- Automatically order scripts for deployment
- Easily rollback changes
- Collaborate with tools you already use

This repository contains the main source code for Liquibase. For more information about the product, see [the main project website](https://www.liquibase.org/).

## Liquibase Automation and Integrations

Liquibase Core works with the following databases: Apache Derby, CockroachDB, Firebird, H2, HSQL, Informix, InterBase, MariaDB, MSSQL, MySQL, Oracle, PostgreSQL, SQLite, Sybase Anywhere, Sybase Enterprise. The databases that require extensions are: [Azure Cosmos DB](https://github.com/liquibase/liquibase-cosmosdb), [Cassandra](https://github.com/liquibase/liquibase-cassandra), [Cache](https://github.com/liquibase/liquibase-cache), [DB2i](https://github.com/liquibase/liquibase-db2i), [Hibernate](https://github.com/liquibase/liquibase-hibernate), [Impala/Hive](https://github.com/eselyavka/liquibase-impala), [MaxDB](https://github.com/liquibase/liquibase-maxdb), [MongoDB](https://github.com/liquibase/liquibase-mongodb), [Redshift](https://github.com/liquibase/liquibase-redshift), [SAP HANA](https://github.com/liquibase/liquibase-hanadb), [SQLFire](https://github.com/liquibase/liquibase-sqlfire), [Snowflake](https://github.com/liquibase/liquibase-snowflake), [Teradata](https://github.com/liquibase/liquibase-teradata), [Vertica](https://github.com/liquibase/liquibase-vertica), [VoltDB](https://github.com/diorman/liquibase-voltdb). See [Liquibase Database Tutorials](https://docs.liquibase.com/workflows/database-setup-tutorials/home.html).

Liquibase can be integrated with Maven, Ant, Gradle, Spring Boot, and other CI/CD tools. You can use Liquibase GitHub Actions, Liquibase and Jenkins with Spinnaker, and many different workflows.

## Real-time monitoring and visibility
Try [Liquibase Hub](https://hub.liquibase.com/?_ga=2.53701608.1389719726.1618234889-1334505871.1617797419) to get real-time information about your deployments, an overview of recent commands for the specific database you’re working on, and a place for your team collaboration.

## Install and Run Liquibase

### System Requirements
Liquibase system requirements can be found on the [Download Liquibase](https://www.liquibase.org/download) page.

### An H2 in-memory database example for CLI
1. [Download and run the appropriate installer](https://www.liquibase.org/download). 
2. Make sure to add Liquibase to your PATH.
3. Copy the included `examples` directory to the needed location.
4. Open your CLI and navigate to your `examples/sql` or `examples/xml` directory.
5. Start the included H2 database with the `./start-h2` command.
6. Run the `liquibase update` command.
7. Optionally, follow the prompt for your email to register for [Liquibase Hub](https://hub.liquibase.com/?_ga=2.53701608.1389719726.1618234889-1334505871.1617797419).
8. Run the `liquibase history` command.
9. If you entered your email, check the report link and the output of the `history` command to see they match. 

See also how to [get started with Liquibase in minutes](https://www.liquibase.org/get-started/quickstart) or refer to our [Liquibase Installation](https://docs.liquibase.com/concepts/installation/home.html) documentation page for more details.

## Documentation

Visit the [Liquibase Documentation](https://docs.liquibase.com/home.html) website to find the information on how Liquibase works.

## Courses

Learn all about Liquibase by taking our free online courses at [Liquibase University](https://learn.liquibase.com/).

## Want to help?

Want to file a bug or improve documentation? Excellent! Read up on our guidelines for [contributing](https://www.liquibase.org/community/index.html)!

### Contribute code 

Use our [step-by-step instructions](https://www.liquibase.org/community/contribute/code) for contributing code to the Liquibase open source project. 

### Join the Liquibase Community

Earn points for your achievements and contributions, collect and show off your badges, add accreditations to your LinkedIn. [Learn more about the pathway to Legend and benefits](https://www.liquibase.org/community/liquibase-legends). Enjoy being part of the community!

## Liquibase Extensions

[Provide more database support and features for Liquibase](https://www.liquibase.org/extensions).

## License

Liquibase is [licensed under the Apache 2.0 License](https://github.com/liquibase/liquibase/blob/master/LICENSE.txt).

[Liquibase Pro](https://www.liquibase.com/products/pro?_ga=2.247557831.1389719726.1618234889-1334505871.1617797419), [Liquibase Business](https://www.liquibase.com/products/business?_ga=2.247557831.1389719726.1618234889-1334505871.1617797419), and [Liquibase Enterprise](https://www.liquibase.com/products/enterprise?_ga=2.247557831.1389719726.1618234889-1334505871.1617797419), with additional features and support, is [commercially licensed](https://download.liquibase.org/eula/?_ga=2.247557831.1389719726.1618234889-1334505871.1617797419).

LIQUIBASE is a registered trademark of [Datical](https://www.liquibase.com/company?_ga=2.247557831.1389719726.1618234889-1334505871.1617797419), INC.

## [Contact us](https://www.liquibase.org/contact)

[Liquibase Forum](https://forum.liquibase.org/?__hstc=128893969.04a95a99cefd4bdc8ee741fa374d8f4e.1617800179911.1618490461125.1618499103903.19&__hssc=128893969.1.1618499103903&__hsfp=3590869226&_ga=2.7605618.1389719726.1618234889-1334505871.1617797419) 

[Liquibase Blog](https://www.liquibase.org/blog)
