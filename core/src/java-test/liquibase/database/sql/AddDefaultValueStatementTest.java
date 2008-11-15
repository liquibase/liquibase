package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.MSSQLDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.database.SybaseASADatabase;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.template.JdbcTemplate;
import liquibase.exception.JDBCException;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.util.ISODateFormat;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

public class AddDefaultValueStatementTest extends AbstractSqlStatementTest {

    private static final String TABLE_NAME = "AddDVSTest";
    private static final String COLUMN_NAME = "testCol";

    protected void setupDatabase(Database database) throws Exception {
            dropTableIfExists(null, TABLE_NAME, database);
    }

    protected SqlStatement generateTestStatement() {
        return new AddDefaultValueStatement(null, null, null, null);
    }

    @Test
    public void execute_stringDefaultValue() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {

            public void performTest(Database database) throws Exception {
                new JdbcTemplate(database).execute(new CreateTableStatement(null, TABLE_NAME)
                        .addColumn("id", "int")
                        .addColumn(COLUMN_NAME, "varchar(50)"));

                DatabaseSnapshot snapshot = database.createDatabaseSnapshot(null, null);
                assertNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue());

                new JdbcTemplate(database).execute(new AddDefaultValueStatement(null, TABLE_NAME, COLUMN_NAME, "New Default Value"));

                snapshot = database.createDatabaseSnapshot(null, null);
                assertEquals("New Default Value", snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue());
            }
        });
    }

    @Test
    public void execute_booleanDefaultValue() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new AddDefaultValueStatement(null, TABLE_NAME, COLUMN_NAME, Boolean.TRUE)) {

                    protected void setup(Database database) throws Exception {
                        new JdbcTemplate(database).execute(new CreateTableStatement(null, TABLE_NAME)
                                .addColumn("id", "int")
                                .addColumn(COLUMN_NAME, database.getBooleanType()));
                        super.setup(database);
                    }

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue());
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        if (snapshot.getDatabase() instanceof MySQLDatabase 
                        		|| snapshot.getDatabase() instanceof MSSQLDatabase
                        		|| snapshot.getDatabase() instanceof SybaseASADatabase
                		) {
                            assertEquals("true", snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue().toString().toLowerCase());
                        } else {
                            assertEquals(snapshot.getDatabase().getTrueBooleanValue().toLowerCase(), snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue().toString().toLowerCase());
                        }
                    }

                });
    }

    @Test
    public void execute_intDefaultValue() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new AddDefaultValueStatement(null, TABLE_NAME, COLUMN_NAME, 42)) {

                    protected void setup(Database database) throws Exception {
                        new JdbcTemplate(database).execute(new CreateTableStatement(null, TABLE_NAME)
                                .addColumn("id", "int")
                                .addColumn(COLUMN_NAME, "int"));
                        super.setup(database);
                    }

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue());
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertEquals(new Integer(42), new Integer(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue().toString()));
                    }

                });
    }

    @Test
    public void execute_floatDefaultValue() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new AddDefaultValueStatement(null, TABLE_NAME, COLUMN_NAME, 42.56)) {

                    protected void setup(Database database) throws Exception {
                        new JdbcTemplate(database).execute(new CreateTableStatement(null, TABLE_NAME)
                                .addColumn("id", "int")
                                .addColumn(COLUMN_NAME, "float"));
                        super.setup(database);    //To change body of overridden methods use File | Settings | File Templates.
                    }

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue());
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertEquals(new Float(42.56f), new Float(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue().toString()));
                    }

                });
    }

    @Test
    public void execute_dateTimeDefaultValue() throws Exception {
        final java.sql.Timestamp date = new java.sql.Timestamp(new Date().getTime());

        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new AddDefaultValueStatement(null, TABLE_NAME, COLUMN_NAME, date)) {

                    protected void setup(Database database) throws JDBCException {
                        new JdbcTemplate(database).execute(new CreateTableStatement(null, TABLE_NAME)
                                .addColumn("id", "int")
                                .addColumn(COLUMN_NAME, database.getDateTimeType()));
                    }

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue());
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
                        assertEquals(date.getTime() / 1000, ((Date) snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue()).getTime() / 1000);
                    }

                });
    }

    @Test
    public void execute_dateDefaultValue() throws Exception {
        final java.sql.Date date = new java.sql.Date(new Date().getTime());
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new AddDefaultValueStatement(null, TABLE_NAME, COLUMN_NAME, date)) {

                    protected void setup(Database database) throws Exception {
                        new JdbcTemplate(database).execute(new CreateTableStatement(null, TABLE_NAME)
                                .addColumn("id", "int")
                                .addColumn(COLUMN_NAME, database.getDateType()));

                        super.setup(database);
                    }

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue());
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
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
        final java.sql.Time time = new java.sql.Time(new Date().getTime());
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new AddDefaultValueStatement(null, TABLE_NAME, COLUMN_NAME, time)) {

                    protected void setup(Database database) throws Exception {
                        new JdbcTemplate(database).execute(new CreateTableStatement(null, TABLE_NAME)
                                .addColumn("id", "int")
                                .addColumn(COLUMN_NAME, database.getTimeType()));
                        super.setup(database);
                    }

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).getDefaultValue());
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
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
