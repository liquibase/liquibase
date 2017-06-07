# dbmanul
A change management system for databases based on Liquibase

This fork of the Liquibase source aims to provide:
- More thorough integration testing
- Better Oracle database support
- Supporting different connections for changesets (e.g. use an administrative connection for creating tablespaces and a "regular" connection for normal schema updates)

Current status summary
======================

Last updated: June 7th, 2017

**PLEASE NOTE THAT THE FORK, IN GENERAL, SHOULD BE CONSIDERED UNSTABLE FOR THE MOMENT.**
    A lot of work is currently being done to get all integration tests (OSS + commercial RDBMSs) green; manual testing of features like CDI, RPM/Debian packaging, Spring integration etc. comes after that.   

- General functionality/unit tests: OK
- Status of integration tests for Open Source DBMS (except Firebird, working on that): 
    https://circleci.com/gh/dbmanul/dbmanul
- Issue tracking: https://dbmanul.atlassian.net
- Unmerged PR progress: most upstream prs in the range #678 down to #652 

Database support:
-----------------

**Full support** (DBMS-specific integration tests green, Software should be usable for everyday tasks):
- Apache Derby (tested: 10.13.1.1)
- H2 database (tested: 1.4.195) 
- HyperSQL (hsqldb) (tested: 2.4.0)
- IBM DB2 LUW Express (tested: 10.6) 
- MariaDB (tested: 10.2.6, with InnoDB)
- MySQL (tested: 5.7.18, with InnoDB)
- Oracle Database (tested: 12.1.0.2 and 12.2.0.1)
- PostgreSQL (tested: 9.6)

**Somewhat working** (work in progress):
- Firebird (a problem in snapshotting causes duplicate indexes to be generated to FOREIGN KEY constraints). 
  Regular change sets (forward migration) seems to work fine.
 as deferrable, which is a functionality not present in MySQL/MariaDB AFAIK).
- Microsoft SQL Server (MSSQL) (tested: 2016, Express)
  - Problems with default values and DATE/DATETIME etc. columns
  - Tests running on multiple schemas simultaneously fail
- SAP SQL Anywhere 17 (formerly known as Sybase AS Anywhere)
  - Basic functionality is working
  - Currently failing tests: 
    - runUpdateOnOldChangelogTableFormat (should not affect new users) 
    - Change logs working on more than one schema simultaneously
- SQLite: Generally working except for operations that would be possible with ALTER TABLE on other
    RDBMS (see https://dbmanul.atlassian.net/browse/DBM-3 for details)

**Unstable**
  multiple integration tests to fail
- IBM Informix (work started, but several SQL generators are broken; needs considerable work to get it back again)
  Currently completely broken due to a regression involving catalog and schema names

**Untested / Work has not started yet:**
- SAP Adaptive Server Enterprise (ASE) (formely known as Sybase Adaptive Server)
- Microsoft SQL Server (in case-sensitive mode)
- Microsoft SQL Server (via JTDS driver)
- Determine the oldest supported versions of the RDBMSs, procure them, and test compatibility 

Functionally beyond regular changesets:
---------------------------------------

- Snapshotting can only retrieve attributes that are present in most RDBMS (e.g. names of columns, PKs etc.)
  Attributes that are specific to a RDBMS (like storage attributes for segments in Oracle) cannot be 
  snapshotted currently.
- RPM packaging works, there is a small bug if non-release-version directories are used
- Debian packaging could work (DEBs get generated), but need to set up a Debian box to test it
- Documentation, website: Not yet updated / reviewed.

Currently untested functionality:
---------------------------------

- CDI
- Database documentation generation (DbDoc - though automated tests seem to work)