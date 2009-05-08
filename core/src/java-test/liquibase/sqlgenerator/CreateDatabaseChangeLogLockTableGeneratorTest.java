package liquibase.sqlgenerator;

import liquibase.statement.SqlStatement;
import liquibase.statement.CreateDatabaseChangeLogLockTableStatement;
import liquibase.database.*;

import java.util.List;

import org.junit.Test;

public class CreateDatabaseChangeLogLockTableGeneratorTest extends AbstractSqlGeneratorTest<CreateDatabaseChangeLogLockTableStatement> {

    public CreateDatabaseChangeLogLockTableGeneratorTest() throws Exception {
        super(new CreateDatabaseChangeLogLockTableGenerator());
    }

    protected CreateDatabaseChangeLogLockTableStatement createSampleSqlStatement() {
        return new CreateDatabaseChangeLogLockTableStatement();
    }

    protected List<? extends SqlStatement> setupStatements(Database database) {
        return null;
    }

    @Test
    public void generate() throws Exception {
        CreateDatabaseChangeLogLockTableStatement statement = new CreateDatabaseChangeLogLockTableStatement();
        testSqlOnAllExcept("create table [databasechangeloglock] (" +
                "[id] int not null, " +
                "[locked] boolean not null, " +
                "[lockgranted] datetime, " +
                "[lockedby] varchar(255), " +
                "constraint [pk_databasechangeloglock] primary key ([id]))", statement, SybaseDatabase.class, SybaseASADatabase.class, InformixDatabase.class, MSSQLDatabase.class, DB2Database.class, SQLiteDatabase.class);

        testSqlOn("create table [databasechangeloglock] (" +
                "[id] int not null, " +
                "[locked] boolean not null, " +
                "[lockgranted] datetime, " +
                "[lockedby] text, " +
                "constraint [pk_databasechangeloglock] primary key ([id]))", statement, SQLiteDatabase.class);

        testSqlOn("create table [databasechangeloglock] (" +
                "[id] int not null, " +
                "[locked] bit not null, " +
                "[lockgranted] datetime null, " +
                "[lockedby] varchar(255) null, " +
                "constraint [pk_databasechangeloglock] primary key ([id]))", statement, SybaseDatabase.class, SybaseASADatabase.class);

        testSqlOn("create table [databasechangeloglock] (" +
                "[id] int not null primary key, " +
                "[locked] boolean not null, " +
                "[lockgranted] datetime, " +
                "[lockedby] varchar(255))", statement, InformixDatabase.class);

        testSqlOn("create table [dbo].[databasechangeloglock] (" +
                "[id] int not null, " +
                "[locked] boolean not null, " +
                "[lockgranted] datetime, " +
                "[lockedby] varchar(255), " +
                "constraint [pk_databasechangeloglock] primary key ([id]))", statement, MSSQLDatabase.class);

        testSqlOn("create table [databasechangeloglock] (" +
                "[id] int not null, " +
                "[locked] smallint not null, " +
                "[lockgranted] timestamp, " +
                "[lockedby] varchar(255), " +
                "constraint [pk_databasechange] primary key ([id]))", statement, DB2Database.class);
    }
}
