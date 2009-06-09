package liquibase.statementexecute;

import liquibase.database.*;
import liquibase.database.core.*;
import liquibase.statement.SqlStatement;
import liquibase.statement.CreateDatabaseChangeLogLockTableStatement;

import java.util.List;

import org.junit.Test;

public class CreateDatabaseChangeLogLockTableExecuteTest extends AbstractExecuteTest {
    @Override
    protected List<? extends SqlStatement> setupStatements(Database database) {
        return null;
    }

    @Test
    public void generate() throws Exception {
        this.statementUnderTest = new CreateDatabaseChangeLogLockTableStatement();

        assertCorrect("create table [databasechangeloglock] (" +
                "[id] int not null, " +
                "[locked] boolean not null, " +
                "[lockgranted] datetime, " +
                "[lockedby] text, " +
                "constraint [pk_databasechangeloglock] primary key ([id]))", SQLiteDatabase.class);

        assertCorrect("create table [databasechangeloglock] (" +
                "[id] int not null, " +
                "[locked] bit not null, " +
                "[lockgranted] datetime null, " +
                "[lockedby] varchar(255) null, " +
                "constraint [pk_databasechangeloglock] primary key ([id]))", SybaseDatabase.class, SybaseASADatabase.class);

        assertCorrect("create table [databasechangeloglock] (" +
                "[id] int not null primary key, " +
                "[locked] boolean not null, " +
                "[lockgranted] datetime, " +
                "[lockedby] varchar(255))", InformixDatabase.class);

        assertCorrect("create table [dbo].[databasechangeloglock] (" +
                "[id] int not null, " +
                "[locked] boolean not null, " +
                "[lockgranted] datetime, " +
                "[lockedby] varchar(255), " +
                "constraint [pk_databasechangeloglock] primary key ([id]))", MSSQLDatabase.class);

        assertCorrect("create table [databasechangeloglock] (" +
                "[id] int not null, " +
                "[locked] smallint not null, " +
                "[lockgranted] timestamp, " +
                "[lockedby] varchar(255), " +
                "constraint [pk_databasechange] primary key ([id]))", DB2Database.class);

        assertCorrect("create table [databasechangeloglock] (" +
                "[id] int not null, " +
                "[locked] boolean not null, " +
                "[lockgranted] datetime, " +
                "[lockedby] varchar(255), " +
                "constraint [pk_databasechangeloglock] primary key ([id]))");

    }
}
