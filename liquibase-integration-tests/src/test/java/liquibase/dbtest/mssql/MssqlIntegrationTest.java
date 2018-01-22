package liquibase.dbtest.mssql;

import liquibase.CatalogAndSchema;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.MSSQLDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.DatabaseFunction;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Vector;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeNotNull;

public class MssqlIntegrationTest extends AbstractMssqlIntegrationTest {

    public MssqlIntegrationTest() throws Exception {
        super("mssql", DatabaseFactory.getInstance().getDatabase("mssql"));
    }

    @Override
    protected boolean supportsAltCatalogTests() {
        return false;
    }

    @Test
    public void defaultValuesTests() throws Exception {
        clearDatabase();

        assumeNotNull(this.getDatabase());

        Liquibase liquibase = createLiquibase("changelogs/mssql/issues/default.values.xml");
        liquibase.update((String) null);

        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(CatalogAndSchema.DEFAULT, this.getDatabase(), new SnapshotControl(getDatabase()));

        Vector<Integer> expectedMicrosecValues = new Vector<>();
        expectedMicrosecValues.add(0);
        expectedMicrosecValues.add(123000000);
        expectedMicrosecValues.add(123456700);
        for (Table table : snapshot.get(Table.class)) {
            for (Column column : table.getColumns()) {
                if (column.getName().toLowerCase().endsWith("_default")) {
                    Object defaultValue = column.getDefaultValue();
                    assertNotNull("Null default value for " + table.getName() + "." + column.getName(), defaultValue);
                    if (column.getName().toLowerCase().contains("date") || column.getName().toLowerCase().contains("time")) {
                        if (defaultValue instanceof String) {
                            assertTrue(defaultValue.equals("2017-12-09 23:52:39.1234567 +01:00"));
                        } else {
                            assertTrue("Unexpected default type "+defaultValue.getClass().getName()+" for " + table.getName() + "." + column.getName(), defaultValue instanceof Date);
                            LocalDateTime dtt = null;
                            if (defaultValue instanceof java.sql.Timestamp) {
                                dtt = ((Timestamp)defaultValue).toLocalDateTime();
                            }
                            if (defaultValue instanceof java.sql.Date) {
                                java.sql.Date tmpDate = (java.sql.Date)defaultValue;
                                dtt =  LocalDateTime.of(tmpDate.toLocalDate(),  LocalTime.of(0, 0));
                            }
                            if (defaultValue instanceof java.sql.Time) {
                                java.sql.Time tmpDate = (java.sql.Time)defaultValue;
                                dtt =  LocalDateTime.of(LocalDate.of(2017, 12, 9),
                                    tmpDate.toLocalTime());
                            }

                            assertTrue( expectedMicrosecValues.contains( dtt.getNano() ));
                            assertEquals(dtt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                + " is the 9th day of the month.", 9, dtt.getDayOfMonth() );
                            assertEquals(dtt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                + " is the 12th month (December).",12, dtt.getMonthValue() );
                            assertEquals(dtt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                + " is the year 2017.",2017, dtt.getYear());
                        }
                    } else if (column.getName().toLowerCase().contains("char_")) {
                        assertTrue("Unexpected default type "+defaultValue.getClass().getName()+" for " + table.getName() + "." + column.getName(), defaultValue instanceof String);
                    } else if (column.getName().toLowerCase().contains("binary_")) {
                        assertTrue("Unexpected default type "+defaultValue.getClass().getName()+" for " + table.getName() + "." + column.getName(), defaultValue instanceof DatabaseFunction);
                    } else {
                        assertTrue("Unexpected default type "+defaultValue.getClass().getName()+" for " + table.getName() + "." + column.getName(), defaultValue instanceof Number);
                        assertEquals(1, ((Number) defaultValue).intValue());
                    }
                }
            }
        }
    }

    @Test
    public void dataTypesTest() throws Exception {
        assumeNotNull(this.getDatabase());
        clearDatabase();

        Liquibase liquibase = createLiquibase("changelogs/mssql/issues/data.types.xml");
        liquibase.update((String) null);

        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(CatalogAndSchema.DEFAULT, this.getDatabase(), new SnapshotControl(getDatabase()));

        for (Table table : snapshot.get(Table.class)) {
            if (getDatabase().isLiquibaseObject(table)) {
                continue;
            }
            for (Column column : table.getColumns()) {
                String expectedType = column.getName().split("_")[0];

                switch(expectedType.toUpperCase()) {
                    // See https://docs.microsoft.com/en-us/sql/t-sql/data-types/ntext-text-and-image-transact-sql
                    // Types text, ntext and image are deprecated and should be translated into
                    // varchar(max), nvarchar(max) and varbinary(max).
                    case "TEXT":
                        expectedType="varchar";
                        break;
                    case "NTEXT":
                        expectedType="nvarchar";
                        break;
                    case "IMAGE":
                        expectedType="varbinary";
                        break;
                    default:
                        // nothing to do
                }

                String foundTypeDefinition = DataTypeFactory.getInstance().from(column.getType(), new MSSQLDatabase()).toDatabaseDataType(getDatabase()).toString();
                // [varbinary] -> varbinary
                foundTypeDefinition = foundTypeDefinition.replaceFirst("^\\[(.*?)\\]", "$1");
                String foundType = foundTypeDefinition.replaceFirst("\\(.*", "").trim();

                assertEquals("Wrong data type for " + table.getName() + "." + column.getName(),
                    expectedType.toLowerCase(),
                    foundType.toLowerCase()
                );

                if ("varbinary".equalsIgnoreCase(expectedType)) {
                    if (column.getName().endsWith("_MAX")) {
                        assertEquals("VARBINARY(MAX)", foundTypeDefinition.toUpperCase());
                    } else {
                        assertEquals("VARBINARY(1)", foundTypeDefinition.toUpperCase());
                    }
                }
            }
        }
    }


    @Test
    /**
     * When snapshotting an MSSQL database, size information is included for
     * XML, SMALLMONEY, HIERARCHYID, DATETIME2, IMAGE, and DATETIMEOFFSET even when the default precisions (if
     * applicable at all) are used. Default sizes/precisions should not be transferred into resulting ChangeLogs/
     * snapshots.
     *
     * Reference: https://liquibase.jira.com/browse/CORE-1515
     */
    public void dataTypeParamsTest() throws Exception {
        assumeNotNull(this.getDatabase());
        clearDatabase();

        Liquibase liquibase = createLiquibase("changelogs/mssql/issues/data.type.params.xml");
        liquibase.update((String) null);

        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(CatalogAndSchema.DEFAULT, this.getDatabase(), new SnapshotControl(getDatabase()));

        for (Table table : snapshot.get(Table.class)) {
            if (getDatabase().isLiquibaseObject(table)) {
                continue;
            }
            for (Column column : table.getColumns()) {
                String expectedType = column.getName().split("_")[0];

                String foundTypeDefinition = DataTypeFactory.getInstance().from(column.getType(), new MSSQLDatabase()).toDatabaseDataType(getDatabase()).toString();
                assertFalse("Parameter found in " + table.getName() + "." + column.getName(), foundTypeDefinition.contains("("));
            }
        }
    }
}
