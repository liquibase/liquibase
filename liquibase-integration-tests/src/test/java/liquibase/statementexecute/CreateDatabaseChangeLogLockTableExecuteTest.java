package liquibase.statementexecute;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.Db2zDatabase;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MariaDBDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateDatabaseChangeLogLockTableStatement;

public class CreateDatabaseChangeLogLockTableExecuteTest extends AbstractExecuteTest {
    @Override
    protected List<? extends SqlStatement> setupStatements(Database database) {
        return null;
    }

    @Before
    public void init() {
        this.statementUnderTest = new CreateDatabaseChangeLogLockTableStatement();
    }

    @Test
    public void generateSQLite() throws Exception {

        assertCorrect(new String[]{"create table [databasechangeloglock] (" +
                "[id] int not null, " +
                "[locked] boolean not null, " +
                "[lockgranted] datetime, " +
                "[lockedby] text, " +
                "constraint [pk_databasechangeloglock] primary key ([id]))"}, SQLiteDatabase.class);
    }


    @Test
    public void generateSybase() throws Exception {

        assertCorrect(new String[]{"create table [databasechangeloglock] (" +
                "[id] int not null, " +
                "[locked] bit not null, " +
                "[lockgranted] datetime null, " +
                "[lockexpires] datetime null, " +
                "[lockedby] varchar(255) null, " +
                "[lockedbyid] varchar(36) null, " +
                "constraint [pk_databasechangeloglock] primary key ([id]))"}, SybaseDatabase.class);
    }


    @Test
    public void generateSybaseASADatabase() throws Exception {
        assertCorrect(new String[]{"create table [databasechangeloglock] (" +
                "[id] int not null, " +
                "[locked] bit not null, " +
                "[lockgranted] datetime null, " +
                "[lockexpires] datetime null, " +
                "[lockedby] varchar(255) null, " +
                "[lockedbyid] varchar(36) null, " +
                "constraint [pk_databasechangeloglock] primary key ([id]))"}, SybaseASADatabase.class);
    }


    @Test
    public void generateInformixDatabase() throws Exception {
        assertCorrect(new String[]{"create table [databasechangeloglock] (" +
                "[id] int not null, " +
                "[locked] boolean not null, " +
                "[lockgranted] datetime, " +
                "[lockexpires] datetime, " +
                "[lockedby] varchar(255), " +
                "[lockedbyid] varchar(36), " +
                "primary key (id))"}, InformixDatabase.class);
    }


    @Test
    public void generateMSSQLDatabase() throws Exception {
        assertCorrect(new String[]{"create table [databasechangeloglock] (" +
                "[id] [int] not null, " +
                "[locked] [bit] not null, " +
                "[lockgranted] [datetime2](3), " +
                "[lockexpires] [datetime2](3), " +
                "[lockedby] [nvarchar](255), " +
                "[lockedbyid] [nvarchar](36), " +
                "constraint [pk_databasechangeloglock] primary key ([id]))"}, MSSQLDatabase.class);
    }


    @Test
    public void generateDB2Database() throws Exception {
        assertCorrect(new String[]{"create table [databasechangeloglock] (" +
                "[id] integer not null, " +
                "[locked] smallint not null, " +
                "[lockgranted] timestamp, " +
                "[lockexpires] datetime, " +
                "[lockedby] varchar(255), " +
                "[lockedbyid] varchar(36), " +
                "constraint [pk_dbchgloglock] primary key ([id]))"}, DB2Database.class);
    }


    @Test
    public void generateOracleDatabase() throws Exception {
        assertCorrect(new String[]{"create table databasechangeloglock (" +
                "id number(10, 0) not null, " +
                "locked number(1) not null, " +
                "lockgranted timestamp, " +
                "[lockexpires] timestamp, " +
                "[lockedby] varchar2(255), " +
                "[lockedbyid] varchar2(36), " +
                "constraint pk_databasechangeloglock primary key (id))"}, OracleDatabase.class);
    }


    @Test
    public void generateMySQLDatabase() throws Exception {
        assertCorrect(new String[]{"create table [databasechangeloglock] (" +
                "[id] int not null, " +
                "[locked] boolean not null, " +
                "[lockgranted] datetime null, " +
                "[lockexpires] datetime null, " +
                "[lockedby] varchar(255) null, " +
                "[lockedbyid] varchar(36) null, " +
                "constraint [pk_databasechangeloglock] primary key ([id]))"}, MySQLDatabase.class);
    }


    @Test
    public void generateMariaDBDatabase() throws Exception {
        assertCorrect(new String[]{"create table [databasechangeloglock] (" +
                "[id] int not null, " +
                "[locked] boolean not null, " +
                "[lockgranted] datetime null, " +
                "[lockexpires] datetime null, " +
                "[lockedby] varchar(255) null, " +
                "[lockedbyid] varchar(36) null, " +
                "constraint [pk_databasechangeloglock] primary key ([id]))"}, MariaDBDatabase.class);
    }


    @Test
    public void generatePostgresDatabase() throws Exception {
        assertCorrect(new String[]{"create table [databasechangeloglock] (" +
                "[id] int not null, " +
                "[locked] boolean not null, " +
                "[lockgranted] datetime, " +
                "[lockexpires] timestamp without time zone, " +
                "[lockedby] varchar(255), " +
                "[lockedbyid] varchar(36), " +
                "constraint [databasechangeloglock_pkey] primary key ([id]))"}, PostgresDatabase.class);
    }


    @Test
    public void generateDb2zDatabase() throws Exception {
        assertCorrect(new String[]{"create table [databasechangeloglock] (" +
                "[id] int not null, " +
                "[locked] boolean not null, " +
                "[lockgranted] datetime, " +
                "[lockexpires] timestamp, " +
                "[lockedby] varchar(255), " +
                "[lockedbyid] varchar(36), " +
                "constraint [pk_dbchgloglock] primary key ([id]))"}, Db2zDatabase.class);
    }


    @Test
    public void generateOtherDatabases() throws Exception {
        // all other RDBMS
        testOnAllExcept("create table [databasechangeloglock] (" +
                        "[id] int not null, " +
                        "[locked] boolean not null, " +
                        "[lockgranted] datetime, " +
                        "[lockexpires] datetime, " +
                        "[lockedby] varchar(255), " +
                        "[lockedbyid] varchar(36), " +
                        "constraint [pk_databasechangeloglock] primary key ([id]))",
                SQLiteDatabase.class,
                SybaseDatabase.class,
                SybaseASADatabase.class,
                InformixDatabase.class,
                MSSQLDatabase.class,
                DB2Database.class,
                OracleDatabase.class,
                MySQLDatabase.class,
                MariaDBDatabase.class,
                PostgresDatabase.class,
                Db2zDatabase.class);
    }

}



