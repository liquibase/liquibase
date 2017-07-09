DB-Manul (based on Liquibase) Changelog for version 0.1
=======================================================

Bug fixes:
----------

Bug identifiers starting with [CORE-...] refer to entries in the Liquibase bug database
(https://liquibase.jira.com/projects/CORE/issues). Bug identifiers with the prefix [DBM-...] refer to the DB-Manul
bug list (https://dbmanul.atlassian.net).

### Most or all supported databases:

#### False behaviour / wrong results

- When creating a Index using the <createIndex> change type, the sort order for the columns was ignored. This was
  fixed. However, Firebird SQL only supports indexes with all columns ordered either ascending or descending; when
  trying to mix, DB-Manul will echo an error message.

- When a warning during validation occured at the change set level, it previously was not printed at all
  (commit b73667d0630ebe0335c1472d999c5d67191fe51f)

- Invalid output could be written for change set preconditions when serialising into the YAML format. This was
  fixed thanks to Kamil Kozioł (9a6e9dcb25ff8bf1ce6246b99ad338411b4479c1)

- A performance problem involving String processing was fixed by Olivier Chédru
  (6c1e49f70adb6517266f965089d60dc505501e9c)
  
- When creating a differential report or a differential change set of a Microsoft SQL Server, SAP SQL Anywhere or SAP 
  Adaptive Server (ASE) schema, the order of columns could be falsely reported as deviating if columns were ever 
  dropped any of the the compared tables.
  
- When a changelog file was included by another changelog and the file did not exist, no warning or error was shown 
  if the file name in the `<include>` tag did not have an extension. Thanks to Tomas Dvorak for the bugfix
  (5608dfccf3c50f321d140b3d5f75059f8ce432ac).

- [CORE-1852] checksum depends on line endings and startCommentSymbol was fixed by Ivan Melnikov (upstream pr#648)

- [CORE-2989]: When using a custom change, a wrong confirmation message was returned. Thanks to Eugen Dinca for the
  fix.
  
- When specifying the attribute "objectQuotingStrategy" for a change set, the attribute was applied during the 
  execution phase, but not during the validation phase. Thanks to GitHub user "mway-dmueller" for the fix
  (3965d341a2046cd22a8cf578b16199652442f39c).
  
- [CORE-2944] Honor `outputDefaultSchema=false` and `outputDefaultCatalog=false` when generating SQL code 
  did not work. Thanks to Piotrek Bzdyl for the bug fix.   

#### Crashes and Java exceptions:
- A NullPointerException that could arise when using the Oracle Database BFILE type or the MySQL TIMESTAMP type
  was fixed thanks to "yuxiaobin" (0ea5040caf0175fc36fcaf5b2d375903c777c5eb).

- A Java exception that could occur when accessing a change log inside a JAR file in combination with relative
  path names was fixed thanks to Robert Schulte (c41b250a001ded566a2a1d3ff1532b6743ad8760)

- A crash that could occur when connecting to an SAP SQL Anywhere (formerly Siebel AS Anywhere) database using
  the SAP JDBC driver was fixed thanks to Andreas Pohl (eaa4604b529631b2a1b99aacd5e00ef5c1dd2191)

- [CORE-3014] an empty column description is given in a `<createIndex>` operation, the program crashed with
  a NullPointerException. Thanks to Tom Hombergs for the bugfix (e567bf5eef2764e2cf7d2e8d4bb21372e8aa3e77)
  
- [CORE-3022] a problem with the wrong use of a classloader could cause in a ClassNotFound exception when 
  DB-Manul is used by other software. This was fixed by Roman Fürst (b6fa2263d2db2a23d42369ee30b0cd11031c22ec)
  
### Database-specific bug fixes

#### HyperSQL
- The Liquibase macro current_datetime for a column default (replaced by the DBMS-specific function for getting the
  current date and/or time) did not work in HyperSQL. This was fixed in commit 23ef71b3a2c6ef872d7c27ef55f67cf4cc1b65ee
- When snapshotting a HyperSQL column of the TIMESTAMP type that had a literal (e.g. TIMESTAMP'2017-05-27 18:34:45')
  as default value, incorrect SQL was generated. This was fixed in commit 8148b5bca5f6905c6b1bfa4617ffd4a7b202cca1.
- The keyword SUM was not recognized as a reserved HSQLDB keyword [CORE-3076]. Thanks to GitHub user "TheLegion"
  for the fix (upstream pr#684)

#### Firebird SQL
- Fixed various problems in snapshotting Firebird databases. The root cause was that the data dictionary views (RDB$)
  in Firebird show identifiers (like table/column/index/... names) in CHAR columns, and these are right-padded with
  spaces. This caused various comparisons to fail and, as a consequence, invalid snapshot change sets and/or SQL
  was generated.

#### IBM DB2
- [CORE-2773] Detection of DB2 on AS400 did not work properly - fixed thanks to Martin Aberle 
  (f425f011638db46b0bdc6a680e2d2e5f014feb8d)
- Enumeration of Views in a DB2 database might return objects that are not actually views - fixed thanks to 
  Mårten Svantesson (Upstream pr #686, Commit 0048fed2331803ae7a12078149faae4395069e76)
- [CORE-3005] dropAll on IBM DB2 on AS400 could drop system views - fixed thanks to Martin Aberle
  (60adb8f56fa2d13036f5ae33139dd3f0169e233b)

#### IBM Informix
- Fixed an issue where PRIMARY KEYs might not get generated. This problem might be related to upstream bug
  "[CORE-1775] Informix error when creating primary key"

#### Microsoft SQL Server
- When snapshotting a SQL Server database, invalid `[].` prefixes that appear in front of identifiers could be generated
  due to a problem in schema name processing. This caused invalid SQL to be generated. The problem was fixed in
  commit cabacfe3593324f5223e53496d80c894f48abfa2.

#### MySQL / MariaDB
 - [CORE-3040] - onlyUpdate="true" flag generates empty statements for MySQL DB. Thanks to Luciano Boschi
   for the bug fix (upstream pr#682, commit 58b2839feefd1a003e6555c3761bd0e28c64cc8d). 

#### Oracle Database
- In some situations, valid TIMESTAMP and DATE literals were not recognized. This was fixed in commit
  c307b0795a01086a60e89026a548745c54c1a79e and affected at least Oracle DB.

#### PostgreSQL
- When creating columns with an auto-increment function in PostgreSQL, it was possible for invalid SQL to be generated.
  This was fixed by commit 88f76fc67f10f17e02cd2909fea876ff904777c4.
- When snapshotting a PostgreSQL schema with a column of type "oid", the data type was wrongly snapshotted as
  "oid(16)", which causes an SQL syntax error later. It is now snapshotted as simply "oid"
  (commit a96e3c84785f693d25063538257cc0c8cf0deced)

#### SAP SQL Anywhere (formerly Sybase AS Anywhere)
- Sequences are now supported thanks to Andreas Pohl (b7bd7fc0f7fc6e96e9990dc169507614f861ed05)
- In the operation `<dropUniqueConstraint>`, wrong sql in the form `ALTER TABLE ... DROP UNIOQUE (null)` was 
  generated (c133369a5b6f0530d3cdd862a5bac3edbfd69319)
- [CORE-3009] Wrong SQL was generated for the dropDefaultValue operation, fixed by Andreas Pohl (968bd791c91f6caa36271346ce66537260d9fbc9 )


New features / enhancements
---------------------------

### All supported databases:
- When diffing database objects, the filters includeObjects and excludeObjects are now applied to the list of objects
  that are compared. ([CORE-3079], fixed by Mårten Svantesson)

#### Logging

- Completely new logging system during ChangeSet processing produces (hopefully) better human-readable output.
- Logging only outputs errors and warnings to STDERR now; regular messages are printed to STDOUT. This should make
log files more readable.
- The new log level SQL (between DEBUG and INFO) now prints everything INFO does, plus all native SQL statements
sent to a database instance.

### IBM DB2

- [CORE-2993] Specifying the ORDERED attribute for a `<createSequence>` operation works now thanks to Martin Aberle 
  (b5717eb7a6482ccf204d4bfc0656a8564a67e4e0)

### Oracle Database

- Oracle supports names for NOT NULL constraints. If you want to name yours, these names can now be specified when
  creating them using the new attribute notNullConstraintName.

    <changeSet id="notNullConstraintNamingTest" author="abuschka">
        <createTable tableName="NOTNULL_NAMING_TEST">
            <column name="id" type="int"/>
            <column name="testcol" type="varchar2(50 char)">
                <constraints nullable="false" notNullConstraintName="NN_NAMING_TEST_TESTCOL_NN" />
            </column>
            <column name="testcol2" type="int" />
        </createTable>
        <addNotNullConstraint tableName="NOTNULL_NAMING_TEST" columnName="testcol2" constraintName="NN_NAMING_TEST_TESTCOL2_NN" />
    </changeSet>

- Snapshotting an Oracle schema now extracts the TABLESPACE property of each table (if it is not the default
  tablespace for the connecting user).

### PostgreSQL

- [CORE-2977] Generated primary key constraint name doesn't match Postgres default was implemented by 
  Dario Sneidermanis. DB-Manul will now auto-generate pgsql PK constraint names in the same way as pgsql does
  (table name_pk).

### Miscellaneous

- When snapshotting a schema, a new command line parameter --overwriteOutputFile=true allows overwriting the
  target output file if the user really wants to (e.g. for scripting).

- Bookkeeping of executed change sets:
    - The full hierarchy of context expressions is now persisted, thanks to Yura Nosenko
      (d45a41471b17f9e78e211d75d31452239c218d7c)

- CDI:
  - @TODO: detailed description of upstream pr #678 by islonik/Nikita Lipatov


Incompatibilities
-----------------

### Issues affecting some or all supported databases

- Contrary to Liquibase, DB-Manul tries to include as much metadata in the diff changelogs as possible, and
  will include catalog and schema names for all objects (unless they are the default catalog and/or
  schema of the database user making the snapshot). For tables and indexes, tablespace names are included by
  default. However, if not desired, these options can be switched off using the command line parameters
  `--includeCatalog=false`, `--includeSchema=false` and/or `--includeTablespace=false`.

- Change sets targeted at MySQL/MariaDB and MS SQL Server must now specify tableName in dropIndex changes
  (although omitting this was allowed before, it produced syntax errors in the generated SQL)

### DB-Manul support policy for older DBMS software versions

Unfortunately, supporting very old versions of DBMS is extremely difficult because of these factors:

1. The difficulty in obtaining old software versions (especially of commercial RDBMS)
2. The difficulty in setting up an effective integration test environment to the dependencies that are increasingly
   harder to satisfy.
3. The difficulty in keeping the program code readable - the more "if feature X is not supported in version Y yet,
   we have to to use workaround Z" clauses there are, the higher the risks of bugs.

Due to this, please understand that the general policy is to support only DBMS which are not completely EOL yet
(e.g. for Oracle Database, we try to support all versions that at least still have Extended Support). DB-Manul
will refuse to connect to unsupported software versions to prevent schema corruption from SQL statements that have
adverse effects in old software versions.

### Microsoft SQL Server

- Microsoft SQL Server versions before SQL Server 2008 are not supported anymore and DB-Manul will refuse to connect
  to old instances. Also, SQL Server <2008 is now (2017-05-26) completely desupported by Microsoft.

- When snapshotting a Microsoft SQL Server database or using the generic data type names in change sets, DB-Manul will
  map the deprecated column types text, ntext and image into varchar(max), nvarchar(max) and varbinary(max) column
  types according to the recommendations at
  https://docs.microsoft.com/en-us/sql/t-sql/data-types/ntext-text-and-image-transact-sql
  (commit cf667c6945a5902c54030146ca6929541b7d10f1)

- Because DB-Manul is based on JDK 8, you must upgrade your JDBC driver to version 6.1.0.jre8 or newer.

### MySQL and MariaDB

- Because MySQL and MariaDB start to diverge (currently only in relatively minor details, but this is expected to
  become worse over time), DB-Manul treats them as two completely separate DBMS. As a consequence, you will
  need to use the MariaDB JDBC driver when connecting to a MariaDB server, and the MySQL JDBC driver when
  connecting to a MySQL server. You can have both JDBC drivers in the classpath and DB-Manul will use the
  right one as long as your JDBC URL is configured correctly.

- Because DB-Manul is based on JDK 8, you must upgrade your MariaDB JDBC driver to version 2.0.2 (includes an
  important bugfix regarding CONSTRAINT metadata) and/or your MySQL J-Connector to version 6.0.6 or higher.

### Oracle Database

- Because DB-Manul is based on JDK 8, you must upgrade your Oracle JDBC thin client to at least version 12.2.0.1.
  Even if you are using this new version, you can connect to any Oracle RDBMS instance that is currently under support
  by Oracle Corporation, i.e. Release 11gR2 or newer. Server version 11.2.0.4 has been tested, connecting to version
  11.2.0.1 (=11gR2 without any patch set updates) could work theoretically, but has not been tested and is not a 
  recommended configuration.

### Firebird SQL

- Because DB-Manul is based on JDK 8, you must upgrade your Firebird SQL JDBC driver "Jaybird" to version 3.0.0
  or newer. 
- **You will most likely need to modify your JDBC connection string due to new requirements by the
  driver** (e.g. declaration of character sets)

### PostgreSQL

- Because DB-Manul is based on JDK 8, you must upgrade your PostgreSQL JDBC driver to version 42.1.1 or newer.
- PostgreSQL versions below 9.2 are desupported by postgresql.org (https://www.postgresql.org/support/versioning/).
  DB-Manul will refuse to connect to versions below 9.2.

### Apache Derby

- Because DB-Manul is based on JDK 8, you must upgrade your Apache Derby JDBC driver to version 10.13.1.1 or newer.

### IBM DB2 (Linux/Unix/Windows)

- The `<dropDefaultValue>` operation will not fail with an SQL error anymore if the column did not have a default 
  value in the first place (i.e. there is nothing to do). This makes the behaviour consistent with other RDBMSs 
  that do not care if the column had a default value before dropping the default 
  (commit b24e88ac3cae362ee23be372e4ed8f66589e0ec2)

### IBM Informix SQL

- If you have used the abstract data type TIME in your change sets, please be aware that DB-Manul will not use the
  INTERVAL type any more, because it is the wrong data type for the intended purpose: an interval can be seen
  as an expression of a duration, but the intention of the TIME data type is to express a fixed point in time
  (hh:mm:ss.fffff) within a calendar day.

  Because using DATETIME instead of INTERVAL can have implications on your application (due to semantic
  differences), please test your application behaviour thoroughly after switching to DB-Manul.

  If you do not want this change, an alternative would be to replace the generic TIME datatype in your change sets
  with the native INTERVAL HOUR TO FRACTION(5). This will ensure the DB-Manul will not try to change the data type.

    Abstract datatype     Liquibase 3.6.0 uses this      DB-Manul uses this
    ############################################################################################################
    DATE                  DATE                           (same)
    DATETIME              DATETIME YEAR TO FRACTION(5)   (same)
    TIME                  INTERVAL HOUR TO FRACTION(5)   DATETIME HOUR TO FRACTION(5)

- To avoid data corruption of non-ASCII characters, DB-Manul **requires a Unicode-enabled DB_LOCALE**, either as
environment variable or in the connection string.

- Because there seems to be no locale-independent way of expressing a DATE default value for a DATE column,
DB-Manul **requires the GL_DATE variable to be set to `%iY-%m-%d`**

  Example for a working JDBC connection string:
  
  Assuming your Informix SQL server runs on localhost, port `9090`. The informix database name is `liquibase`.
  The database is created with Unicode awareness. The instance name of your Informix server is `ol_informix1210`.
  In this case, the following connection string should work (other language/territory locales than `en_us` should
  work as well, as look as the locale as a whole is a Unicode locale).

  `jdbc:informix-sqli://localhost:9090/liquibase:informixserver=ol_informix1210;DB_LOCALE=en_us.utf8;GL_DATE=%iY-%m-%d`

Known bugs and problems
=======================

Regrettably, yes.

## SQLite

### [DBM-3] - Several broken SQLite operations relating to ALTER TABLE code

- The following change types (and their reverse/rollback counterparts) are currently not possible in combination
  with SQLite:
    - renameColumn
    - addNotNullConstraint (-> dropNotNullConstraint)
    - addForeignKeyConstraint (-> dropForeignKeyConstraint)
    - addDefaultValue (-> dropDefaultValue)
    - addPrimaryKey (-> dropPrimaryKey)
    - addUniqueConstraint (-> dropUniqueConstraint)

- Please track this bug at 
  https://dbmanul.atlassian.net/browse/DBM-3 for current information about bug fixing.

Further credits and acknowledgements 
====================================

The maintainer would like to thank the following people:

- Richard Bradley (https://github.com/RichardBradley) for his patches that helped making the integration tests
  work again.
- "alex-on-java" for his code cleanup in SpringLiquibase (c738963deeaa8539a4f0e7419db4cbc27d925171)
- Tom Hombergs for his improvement for an error message during YAML parsing (5e264828ebe28f1ed86cdfc7bfbf4bb6ca05f83f)
- Nathan Voxland for creating Liquibase (http://www.liquibase.org), the basis for DB-Manul
- Gordon Dickens fixed several typos (d05057715c3b0da254ecce3e04b1c55c0dea893e)