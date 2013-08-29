package liquibase.statementexecute;

import liquibase.database.*;
import liquibase.database.core.*;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateDatabaseChangeLogLockTableStatement;

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

        assertCorrect(new String[]{"create table [databasechangeloglock] (" +
                "[id] int not null, " +
                "[locked] boolean not null, " +
                "[lockgranted] datetime, " +
                "[lockedby] text, " +
                "constraint [pk_databasechangeloglock] primary key ([id]))",
                "insert into [databasechangeloglock] ([id], [locked]) values (1, 0)"}, SQLiteDatabase.class);

        assertCorrect(new String[]{"create table [databasechangeloglock] (" +
                "[id] int not null, " +
                "[locked] bit not null, " +
                "[lockgranted] datetime null, " +
                "[lockedby] varchar(255) null, " +
                "constraint [pk_databasechangeloglock] primary key ([id]))",
                "insert into [databasechangeloglock] ([id], [locked]) values (1, 0)"}, SybaseDatabase.class);

        assertCorrect(new String[]{"create table [databasechangeloglock] (" +
                "[id] int not null, " +
                "[locked] bit not null, " +
                "[lockgranted] datetime null, " +
                "[lockedby] varchar(255) null, " +
                "constraint [pk_databasechangeloglock] primary key ([id]))",
                "insert into [databasechangeloglock] ([id], [locked]) values (1, 0)"}, SybaseASADatabase.class);

        assertCorrect(new String[]{"create table [databasechangeloglock] (" +
                "[id] int not null, " +
                "[locked] boolean not null, " +
                "[lockgranted] datetime, " +
                "[lockedby] varchar(255), " +
                "primary key (id))",
                "insert into [databasechangeloglock] ([id], [locked]) values (1, 'f')"}, InformixDatabase.class);

        assertCorrect(new String[]{"create table [databasechangeloglock] (" +
                "[id] int not null, " +
                "[locked] bit not null, " +
                "[lockgranted] datetime, " +
                "[lockedby] varchar(255), " +
                "constraint [pk_databasechangeloglock] primary key ([id]))",
                "insert into [databasechangeloglock] ([id], [locked]) values (1, 0)"}, MSSQLDatabase.class);

        assertCorrect(new String[]{"create table [databasechangeloglock] (" +
                "[id] integer not null, " +
                "[locked] smallint not null, " +
                "[lockgranted] timestamp, " +
                "[lockedby] varchar(255), " +
                "constraint [pk_dbchgloglock] primary key ([id]))",
                "insert into [databasechangeloglock] ([id], [locked]) values (1, 0)"}, DB2Database.class);

        assertCorrect(new String[]{"create table [databasechangeloglock] (" +
                "[id] integer not null, " +
                "[locked] number(1) not null, " +
                "[lockgranted] timestamp, " +
                "[lockedby] varchar2(255), " +
                "constraint [pk_databasechangeloglock] primary key ([id]))",
                "insert into [databasechangeloglock] ([id], [locked]) values (1, 0)"}, OracleDatabase.class);

        assertCorrect(new String[]{"create table [databasechangeloglock] (" +
                "[id] int not null, " +
                "[locked] boolean not null, " +
                "[lockgranted] datetime null, " +
                "[lockedby] varchar(255) null, " +
                "constraint [pk_databasechangeloglock] primary key ([id]))",
                "insert into [databasechangeloglock] ([id], [locked]) values (1, FALSE)"}, MySQLDatabase.class);

        assertCorrect(new String[]{"create table [databasechangeloglock] (" +
                "[id] int not null, " +
                "[locked] boolean not null, " +
                "[lockgranted] datetime, " +
                "[lockedby] varchar(255), " +
                "constraint [pk_dbchgloglock] primary key ([id]))",
                "insert into [databasechangeloglock] ([id], [locked]) values (1, FALSE)"}, DB2iDatabase.class);

        assertCorrect(new String[]{"create table [databasechangeloglock] (" +
                "[id] int not null, " +
                "[locked] boolean not null, " +
                "[lockgranted] datetime, " +
                "[lockedby] varchar(255), " +
                "constraint [pk_databasechangeloglock] primary key ([id]))",
                "insert into [databasechangeloglock] ([id], [locked]) values (1, FALSE)"});

    }
}
