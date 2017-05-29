# dbmanul
A change management system for databases based on Liquibase

This fork of the Liquibase source aims to provide:
- More thorough integration testing
- Better Oracle database support
- Supporting different connections for changesets (e.g. use an administrative connection for creating tablespaces and a "regular" connection for normal schema updates)

Current state:

PLEASE NOTE THAT THE FORK, IN GENERAL, SHOULD BE CONSIDERED UNSTABLE FOR THE MOMENT. A lot of work is currently being done to get all integration tests green; manual testing of features like CDI, RPM/Debian packaging, Spring integration etc. comes after that.   

General functionality/unit tests: OK

Database support:

Full support (DBMS-specific integration tests work, Software should be usable for everyday tasks):
- IBM DB2 LUW Express (currently confirmed versions: v10.6) 
- Apache Derby
- HyperSQL
- Oracle Database (currently confirmed versions: 12.1.0.2 and 12.2.0.1, more to come)

Mostly working:
- Firebird (Snapshot integration test fails, probably bug in Snapshotting functionality present). Rregular change sets seem to work fine.
- PostgreSQL 9.6 Some problems with snapshotting and when using multiple schemas
- MySQL/MariaDB: Problems with default values for DATE columns, snapshot problems (constraints seem to be snapshotted
 as deferrable, which is a functionality not present in MySQL/MariaDB AFAIK).
- MSSQL Server 2016 (Express): Minor problems in snapshotting (changed column order), some problems with default values relating to DATE/DATETIME etc. columns

Unstable:
- H2 (integration test crashes VM with current JDBC driver, multiple failing integration tests)
- Sybase AS Anywhere (work started, but several SQL generators are broken; needs considerable work to get it back again)
- IBM Informix (work started, but several SQL generators are broken; needs considerable work to get it back again)

Untested/Work not started yet:
- Sybase AS
- Microsoft SQL Server (in case-sensitive mode)
- Microsoft SQL Server (via JTDS driver)
- SQLite
- Determe the oldest supported versions of the RDBMSs, procure them, and test compatibility 

Functionally beyond regular changesets:
- Snapshotting in general seems to have a lot of problems and needs additional work to get it stable.
- RPM packaging works, there is a small bug if non-release-version directories are used
- Debian packaging could work (DEBs get generated), but need to set up a Debian box to test it
- Documentation, website: Not yet updated / reviewed.

Currently untested functionality:
- CDI
- Database documentation generation (DbDoc - though automated tests seem to work)

