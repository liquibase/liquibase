package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.MSSQLDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.template.JdbcTemplate;
import liquibase.exception.JDBCException;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.TestContext;
import liquibase.util.ISODateFormat;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

public class AddDefaultValueStatementTest {

    private static final String TABLE_NAME = "AddDVSTest";
    private static final String COLUMN_NAME = "testCol";

    @Before
    @After
    public void dropTable() throws Exception {
        for (Database database : TestContext.getInstance().getAvailableDatabases()) {
            try {
                new JdbcTemplate(database).execute(new RawSqlStatement("drop table " + TABLE_NAME));
            } catch (JDBCException e) {
                if (!database.getAutoCommitMode()) {
                    database.getConnection().rollback();
                }
            }
        }
    }

    @Test
    public void getEndDelimiter() throws Exception {

        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                assertEquals(";", new AddDefaultValueStatement(null, null, null, null).getEndDelimiter(database));
            }
        });
    }

    @Test
    public void execute_stringDefaultValue() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                new JdbcTemplate(database).execute(new CreateTableStatement(null, TABLE_NAME)
                        .addColumn("id", "int")
                        .addColumn(COLUMN_NAME, "varchar(50)"));

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue());

                new JdbcTemplate(database).execute(new AddDefaultValueStatement(null, TABLE_NAME, COLUMN_NAME, "New Default Value"));

                snapshot = new DatabaseSnapshot(database);
                assertEquals("New Default Value", snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue());
            }
        });
    }

    @Test
    public void execute_booleanDefaultValue() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                new JdbcTemplate(database).execute(new CreateTableStatement(null, TABLE_NAME)
                        .addColumn("id", "int")
                        .addColumn(COLUMN_NAME, database.getBooleanType()));

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue());

                new JdbcTemplate(database).execute(new AddDefaultValueStatement(null, TABLE_NAME, COLUMN_NAME, Boolean.TRUE));

                snapshot = new DatabaseSnapshot(database);
                if (database instanceof MySQLDatabase || database instanceof MSSQLDatabase) {
                    assertEquals("true", snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue().toString().toLowerCase());
                } else {
                    assertEquals(database.getTrueBooleanValue().toLowerCase(), snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue().toString().toLowerCase());
                }
            }
        });
    }

    @Test
    public void execute_intDefaultValue() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                new JdbcTemplate(database).execute(new CreateTableStatement(null, TABLE_NAME)
                        .addColumn("id", "int")
                        .addColumn(COLUMN_NAME, "int"));

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue());

                new JdbcTemplate(database).execute(new AddDefaultValueStatement(null, TABLE_NAME, COLUMN_NAME, 42));

                snapshot = new DatabaseSnapshot(database);
                assertEquals(42, new Integer(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue().toString()));
            }
        });
    }

    @Test
    public void execute_floatDefaultValue() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                new JdbcTemplate(database).execute(new CreateTableStatement(null, TABLE_NAME)
                        .addColumn("id", "int")
                        .addColumn(COLUMN_NAME, "float"));

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue());

                new JdbcTemplate(database).execute(new AddDefaultValueStatement(null, TABLE_NAME, COLUMN_NAME, 42.56));

                snapshot = new DatabaseSnapshot(database);
                assertEquals(42.56f, new Float(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue().toString()));
            }
        });
    }

    @Test
    public void execute_dateTimeDefaultValue() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                new JdbcTemplate(database).execute(new CreateTableStatement(null, TABLE_NAME)
                        .addColumn("id", "int")
                        .addColumn(COLUMN_NAME, database.getDateTimeType()));

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue());

                java.sql.Timestamp date = new java.sql.Timestamp(new Date().getTime());

                new JdbcTemplate(database).execute(new AddDefaultValueStatement(null, TABLE_NAME, COLUMN_NAME, date));

                snapshot = new DatabaseSnapshot(database);
                assertEquals(date.getTime() / 1000, ((Date) snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue()).getTime() / 1000);
            }
        });
    }

    @Test
    public void execute_dateDefaultValue() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                new JdbcTemplate(database).execute(new CreateTableStatement(null, TABLE_NAME)
                        .addColumn("id", "int")
                        .addColumn(COLUMN_NAME, database.getDateType()));

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue());

                java.sql.Date date = new java.sql.Date(new Date().getTime());

                new JdbcTemplate(database).execute(new AddDefaultValueStatement(null, TABLE_NAME, COLUMN_NAME, date));

                snapshot = new DatabaseSnapshot(database);
                Object defaultValue = snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue();
                if (defaultValue instanceof java.sql.Date) {
                    assertEquals(new ISODateFormat().format(date), new ISODateFormat().format(((java.sql.Date) defaultValue)));
                } else { //mssql uses smalldatetime for date which is actually a timestamp
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    cal.set(Calendar.HOUR, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    assertEquals(new ISODateFormat().format(new java.sql.Date(cal.getTimeInMillis())), new ISODateFormat().format(new java.sql.Date(((java.sql.Timestamp) defaultValue).getTime())));
                }
            }
        });
    }

    @Test
    public void execute_timeDefaultValue() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                new JdbcTemplate(database).execute(new CreateTableStatement(null, TABLE_NAME)
                        .addColumn("id", "int")
                        .addColumn(COLUMN_NAME, database.getTimeType()));

                DatabaseSnapshot snapshot = new DatabaseSnapshot(database);
                assertNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue());

                java.sql.Time time = new java.sql.Time(new Date().getTime());

                new JdbcTemplate(database).execute(new AddDefaultValueStatement(null, TABLE_NAME, COLUMN_NAME, time));

                snapshot = new DatabaseSnapshot(database);
                Object defaultValue = snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue();
                if (defaultValue instanceof java.sql.Time) {
                    assertEquals(new ISODateFormat().format(time), new ISODateFormat().format(((java.sql.Time) defaultValue)));
                } else { //mssql uses smalldatetime which is a timestamp, oracle uses date which is a date
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(time);
                    cal.set(Calendar.DAY_OF_YEAR, 0);
                    cal.set(Calendar.YEAR, 0);
                    assertEquals(new ISODateFormat().format(new java.sql.Time(cal.getTimeInMillis())), new ISODateFormat().format(new java.sql.Time(((Date) defaultValue).getTime())));
                }
            }
        });
    }
}
