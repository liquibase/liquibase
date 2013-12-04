package liquibase.change;

import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.exception.*;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.serializer.core.string.StringChangeLogSerializer;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.structure.core.View;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.*;
import static liquibase.test.Assert.assertArraysEqual;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/*
I moved this annotation from the class 
@PrepareForTest({SqlGeneratorFactory.class, CheckSum.class})
and used more specific annotations on individual test methods 
because SqlGeneratorFactory was scanning classes and interacting
with large numbers of mocked/generated classes with proxied
methods.  The behavior seemed to differ between Linux and Windows.
I don't really know why yet.
 */


@RunWith(PowerMockRunner.class)
public class AbstractChangeTest {
  
  @BeforeClass
  public static void unClench() {
  }

    @Test
    public void finishInitialization() throws SetupException {
        AbstractChange change = new ExampleAbstractChange();

        change.finishInitialization(); //does nothing
    }

    @Test
    public void createChangeMetaData_noAnnotation() {
        try {
            new AbstractChange() {
                @Override
                public String getConfirmationMessage() {
                    return null;
                }

                @Override
                public SqlStatement[] generateStatements(Database database) {
                    return null;
                }

                @Override
                public String getSerializedObjectNamespace() {
                    return STANDARD_CHANGELOG_NAMESPACE;
                }

            }.createChangeMetaData();
            fail("Did not throw exception");
        } catch (UnexpectedLiquibaseException e) {
            assertTrue("Incorrect message: "+e.getMessage(), e.getMessage().startsWith("liquibase.exception.UnexpectedLiquibaseException: No @DatabaseChange annotation for "));
        }
    }

    @Test
    public void createChangeMetaData() {
        ExampleAbstractChange change = new ExampleAbstractChange();
        ChangeMetaData changeMetaData = change.createChangeMetaData();
        assertEquals("exampleAbstractChange", changeMetaData.getName());
        assertEquals("Used for the AbstractChangeTest unit test", changeMetaData.getDescription());
        assertEquals(1, changeMetaData.getPriority());
        assertNull(null, changeMetaData.getAppliesTo());

        Map<String, ChangeParameterMetaData> parameters = changeMetaData.getParameters();
        assertEquals(3, parameters.size());
        ChangeParameterMetaData dbmsMetaData = parameters.get("dbms");
        ChangeParameterMetaData paramOneMetaData = parameters.get("paramOne");
        ChangeParameterMetaData paramTwoMetaData = parameters.get("paramTwo");
        ChangeParameterMetaData paramNoMetaData = parameters.get("paramNoMetadata");
        ChangeParameterMetaData paramNotIncludedMetaData = parameters.get("paramNotIncluded");
        ChangeParameterMetaData paramNoWriteMethodMetaData = parameters.get("paramNoWriteMethod");

        assertNull(dbmsMetaData);
        assertNotNull(paramOneMetaData);
        assertEquals("paramOne", paramOneMetaData.getParameterName());
        assertEquals("Param One", paramOneMetaData.getDisplayName());
        assertEquals("string", paramOneMetaData.getDataType());
        assertEquals("", paramOneMetaData.getMustEqualExisting());
        assertEquals(0, paramOneMetaData.getRequiredForDatabase().size());

        assertNotNull(paramTwoMetaData);
        assertEquals("paramTwo", paramTwoMetaData.getParameterName());
        assertEquals("Param Two", paramTwoMetaData.getDisplayName());
        assertEquals("integer", paramTwoMetaData.getDataType());
        assertEquals("table", paramTwoMetaData.getMustEqualExisting());
        assertEquals(2, paramTwoMetaData.getRequiredForDatabase().size());
        assertTrue(paramTwoMetaData.getRequiredForDatabase().contains("mysql"));
        assertTrue(paramTwoMetaData.getRequiredForDatabase().contains("mssql"));

        assertNotNull(paramNoMetaData);
        assertNull(paramNotIncludedMetaData);
        assertNull("Properties with no write method should not be included", paramNoWriteMethodMetaData);
    }

    @Test
    public void createChangeMetaData_noParams() {
        ExampleParamlessAbstractChange change = new ExampleParamlessAbstractChange();
        ChangeMetaData changeMetaData = change.createChangeMetaData();

        Map<String, ChangeParameterMetaData> parameters = changeMetaData.getParameters();
        assertEquals(0, parameters.size());
    }

    @Test(expected = UnexpectedLiquibaseException.class)
    public void createChangeParameterMetadata_invalidParamNameEmptyParams() throws Exception {
        new ExampleParamlessAbstractChange().createChangeParameterMetadata("paramOne");
    }

    @Test(expected = UnexpectedLiquibaseException.class)
    public void createChangeParameterMetadata_invalidParamName() throws Exception {
        assertNull(new ExampleAbstractChange().createChangeParameterMetadata("paramInvalid"));
    }

    @Test
    public void getChangeSet() {
        ExampleAbstractChange change = new ExampleAbstractChange();
        ChangeSet changeSet = mock(ChangeSet.class);
        change.setChangeSet(changeSet);

        assertSame(changeSet, change.getChangeSet());

        change.setChangeSet(null);
        assertNull("Should be able to clear out changeset", change.getChangeSet());
    }

    @Test
    @PrepareForTest({SqlGeneratorFactory.class})
    public void generateStatementsVolatile() throws Exception {
        Database database = mock(Database.class);
        final SqlStatement statement1No = mock(SqlStatement.class);
        final SqlStatement statement2No = mock(SqlStatement.class);
        final SqlStatement statement1Yes = mock(SqlStatement.class);
        final SqlStatement statement2Yes = mock(SqlStatement.class);

        SqlGeneratorFactory generatorFactory = mock(SqlGeneratorFactory.class);
        when(generatorFactory.generateStatementsVolatile(statement1No, database)).thenReturn(false);
        when(generatorFactory.generateStatementsVolatile(statement2No, database)).thenReturn(false);
        when(generatorFactory.generateStatementsVolatile(statement1Yes, database)).thenReturn(true);
        when(generatorFactory.generateStatementsVolatile(statement2Yes, database)).thenReturn(true);

        mockStatic(SqlGeneratorFactory.class);
        when(SqlGeneratorFactory.getInstance()).thenReturn(generatorFactory);

        assertTrue(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) { return new SqlStatement[] { statement1Yes, statement2Yes }; }
        }.generateStatementsVolatile(database));

        assertFalse(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) { return new SqlStatement[]{statement1No, statement2No}; }
        }.generateStatementsVolatile(database));

        assertTrue(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) {
                return new SqlStatement[]{statement1No, statement1Yes};
            }
        }.generateStatementsVolatile(database));
    }

    @Test
    public void generateStatementsVolatile_noStatements() throws Exception {
        assertFalse(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) {
                return null;
            }
        }.generateStatementsVolatile(mock(Database.class)));

        assertFalse(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) {
                return new SqlStatement[0];
            }
        }.generateStatementsVolatile(mock(Database.class)));
    }

    @Test
    @PrepareForTest({SqlGeneratorFactory.class})
    public void generateRollbackStatementsVolatile() throws Exception {
        Database database = mock(Database.class);
        final SqlStatement statement1No = mock(SqlStatement.class);
        final SqlStatement statement2No = mock(SqlStatement.class);
        final SqlStatement statement1Yes = mock(SqlStatement.class);
        final SqlStatement statement2Yes = mock(SqlStatement.class);

        SqlGeneratorFactory generatorFactory = mock(SqlGeneratorFactory.class);
        when(generatorFactory.generateRollbackStatementsVolatile(statement1No, database)).thenReturn(false);
        when(generatorFactory.generateRollbackStatementsVolatile(statement2No, database)).thenReturn(false);
        when(generatorFactory.generateRollbackStatementsVolatile(statement1Yes, database)).thenReturn(true);
        when(generatorFactory.generateRollbackStatementsVolatile(statement2Yes, database)).thenReturn(true);

        mockStatic(SqlGeneratorFactory.class);
        when(SqlGeneratorFactory.getInstance()).thenReturn(generatorFactory);

        assertTrue(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) { return new SqlStatement[] { statement1Yes, statement2Yes }; }
        }.generateRollbackStatementsVolatile(database));

        assertFalse(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) { return new SqlStatement[]{statement1No, statement2No}; }
        }.generateRollbackStatementsVolatile(database));

        assertTrue(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) { return new SqlStatement[]{statement1No, statement1Yes}; }
        }.generateRollbackStatementsVolatile(database));
    }

    @Test
    public void generateRollbackStatementsVolatile_noStatements() throws Exception {
        assertFalse(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) {
                return null;
            }
        }.generateRollbackStatementsVolatile(mock(Database.class)));

        assertFalse(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) {
                return new SqlStatement[0];
            }
        }.generateRollbackStatementsVolatile(mock(Database.class)));
    }

    @Test
    public void supports_noStatements() throws Exception {
        assertTrue(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) {
                return null;
            }
        }.supports(mock(Database.class)));

        assertTrue(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) {
                return new SqlStatement[0];
            }
        }.supports(mock(Database.class)));
    }

    @Test
    @PrepareForTest({SqlGeneratorFactory.class})
    public void warn() throws Exception {
        Database database = mock(Database.class);
        final SqlStatement statementUnsupported = mock(SqlStatement.class);
        final SqlStatement statementNullWarnings = mock(SqlStatement.class);
        final SqlStatement statementEmptyWarnings = mock(SqlStatement.class);
        final SqlStatement statementOneWarning = mock(SqlStatement.class);
        final SqlStatement statementTwoWarnings = mock(SqlStatement.class);

        SqlGeneratorFactory generatorFactory = mock(SqlGeneratorFactory.class);
        when(generatorFactory.supports(statementUnsupported, database)).thenReturn(false);
        when(generatorFactory.supports(statementNullWarnings, database)).thenReturn(true);
        when(generatorFactory.supports(statementEmptyWarnings, database)).thenReturn(true);
        when(generatorFactory.supports(statementOneWarning, database)).thenReturn(true);
        when(generatorFactory.supports(statementTwoWarnings, database)).thenReturn(true);

        when(generatorFactory.warn(statementUnsupported, database)).thenReturn(new Warnings().addWarning("A"));
        when(generatorFactory.warn(statementNullWarnings, database)).thenReturn(null);
        when(generatorFactory.warn(statementEmptyWarnings, database)).thenReturn(new Warnings());
        when(generatorFactory.warn(statementOneWarning, database)).thenReturn(new Warnings().addWarning("x"));
        when(generatorFactory.warn(statementTwoWarnings, database)).thenReturn(new Warnings().addWarning("y").addWarning("z"));

        mockStatic(SqlGeneratorFactory.class);
        when(SqlGeneratorFactory.getInstance()).thenReturn(generatorFactory);

        Warnings warningsToTest = new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) {
                return new SqlStatement[]{statementUnsupported, statementNullWarnings, statementEmptyWarnings, statementOneWarning, statementTwoWarnings};
            }
        }.warn(database);

        assertEquals(3, warningsToTest.getMessages().size());
        assertEquals("x", warningsToTest.getMessages().get(0));
        assertEquals("y", warningsToTest.getMessages().get(1));
        assertEquals("z", warningsToTest.getMessages().get(2));
    }

    @Test
    @PrepareForTest({SqlGeneratorFactory.class})
    public void warn_unsupportedButSkip() throws Exception {
        Database database = new MySQLDatabase();
        final SqlStatement statementSkip = mock(SqlStatement.class);
        final SqlStatement statementFails = mock(SqlStatement.class);
        when(statementSkip.skipOnUnsupported()).thenReturn(true);
        when(statementFails.skipOnUnsupported()).thenReturn(true);

        SqlGeneratorFactory generatorFactory = mock(SqlGeneratorFactory.class);
        when(generatorFactory.supports(statementSkip, database)).thenReturn(false);
        when(generatorFactory.supports(statementFails, database)).thenReturn(true);

        when(generatorFactory.warn(statementSkip, database)).thenReturn(new Warnings().addWarning("x"));
        when(generatorFactory.warn(statementFails, database)).thenReturn(new Warnings().addWarning("y"));

        mockStatic(SqlGeneratorFactory.class);
        when(SqlGeneratorFactory.getInstance()).thenReturn(generatorFactory);

        Warnings warnings = new ExampleParamlessAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) {
                return new SqlStatement[]{statementSkip, statementFails};
            }
        }.warn(database);

        assertEquals(2, warnings.getMessages().size());
        assertTrue(warnings.getMessages().get(0), warnings.getMessages().get(0).matches("liquibase.statement.SqlStatement.* is not supported on mysql, but exampleParamelessAbstractChange will still execute"));
        assertEquals("y", warnings.getMessages().get(1));
    }

    @Test
    public void warn_noStatements() throws Exception {
        assertFalse(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) {
                return null;
            }
        }.warn(mock(Database.class)).hasWarnings());

        assertFalse(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) {
                return new SqlStatement[0];
            }
        }.warn(mock(Database.class)).hasWarnings());
    }

    @Test
    public void validate_noStatements() throws Exception {
        assertFalse(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) {
                return null;
            }
        }.validate(mock(Database.class)).hasErrors());

        assertFalse(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) {
                return new SqlStatement[0];
            }
        }.validate(mock(Database.class)).hasErrors());
    }

    @Test
    public void validate_noParameters() throws Exception {
        assertFalse(new ExampleParamlessAbstractChange().validate(mock(Database.class)).hasErrors());
    }

    @Test
    @PrepareForTest({SqlGeneratorFactory.class})
    public void validate_notSupported() throws Exception {
        Database database = new MySQLDatabase();
        final SqlStatement statement1Unsupported = mock(SqlStatement.class);
        final SqlStatement statement2Unsupported = mock(SqlStatement.class);
        final SqlStatement statementSupported = mock(SqlStatement.class);

        SqlGeneratorFactory generatorFactory = mock(SqlGeneratorFactory.class);
        when(generatorFactory.supports(statement1Unsupported, database)).thenReturn(false);
        when(generatorFactory.supports(statement2Unsupported, database)).thenReturn(false);
        when(generatorFactory.supports(statementSupported, database)).thenReturn(true);

        when(generatorFactory.validate(statementSupported, database)).thenReturn(new ValidationErrors());

        mockStatic(SqlGeneratorFactory.class);
        when(SqlGeneratorFactory.getInstance()).thenReturn(generatorFactory);

        ValidationErrors errors = new ExampleParamlessAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) {
                return new SqlStatement[]{statement1Unsupported, statementSupported, statement2Unsupported};
            }
        }.validate(database);

        assertEquals("Should only return one meessage, even through two statements failed", 1, errors.getErrorMessages().size());
        assertEquals("exampleParamelessAbstractChange is not supported on mysql", errors.getErrorMessages().get(0));
    }

    @Test
    @PrepareForTest({SqlGeneratorFactory.class})
    public void validate_statementsHaveErrors() throws Exception {
        Database database = new MySQLDatabase();
        final SqlStatement statement1Fails = mock(SqlStatement.class);
        final SqlStatement statement2Fails = mock(SqlStatement.class);
        final SqlStatement statementNoErrors = mock(SqlStatement.class);

        SqlGeneratorFactory generatorFactory = mock(SqlGeneratorFactory.class);
        when(generatorFactory.supports(any(SqlStatement.class), eq(database))).thenReturn(true);

        when(generatorFactory.validate(statement1Fails, database)).thenReturn(new ValidationErrors().addError("x"));
        when(generatorFactory.validate(statement2Fails, database)).thenReturn(new ValidationErrors().addError("y").addError("z"));
        when(generatorFactory.validate(statementNoErrors, database)).thenReturn(new ValidationErrors());

        mockStatic(SqlGeneratorFactory.class);
        when(SqlGeneratorFactory.getInstance()).thenReturn(generatorFactory);

        ValidationErrors errors = new ExampleParamlessAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) {
                return new SqlStatement[]{statement1Fails, statement2Fails, statementNoErrors};
            }
        }.validate(database);

        assertEquals(3, errors.getErrorMessages().size());
        assertEquals("x", errors.getErrorMessages().get(0));
        assertEquals("y", errors.getErrorMessages().get(1));
        assertEquals("z", errors.getErrorMessages().get(2));
    }

    @Test
    public void validate_missingRequiredValue() throws Exception {
        ExampleAbstractChange change = new ExampleAbstractChange();
        ValidationErrors errors = change.validate(new MSSQLDatabase());
        assertEquals(1, errors.getErrorMessages().size());
        assertEquals("paramTwo is required for exampleAbstractChange on mssql", errors.getErrorMessages().get(0));
    }

    @Test
    public void validate_noErrors() throws Exception {
        ExampleAbstractChange change = new ExampleAbstractChange();
        change.setParamTwo(3);
        ValidationErrors errors = change.validate(new MSSQLDatabase());
        assertFalse(errors.hasErrors());
    }

    @Test
    @PrepareForTest({SqlGeneratorFactory.class})
    public void validate_noErrorsForSkipOnUnsupported() throws Exception {
        Database database = new MySQLDatabase();
        final SqlStatement statementSkip = mock(SqlStatement.class);
        final SqlStatement statementFails = mock(SqlStatement.class);
        when(statementSkip.skipOnUnsupported()).thenReturn(true);
        when(statementFails.skipOnUnsupported()).thenReturn(true);

        SqlGeneratorFactory generatorFactory = mock(SqlGeneratorFactory.class);
        when(generatorFactory.supports(statementSkip, database)).thenReturn(false);
        when(generatorFactory.supports(statementFails, database)).thenReturn(true);

        when(generatorFactory.validate(statementSkip, database)).thenReturn(new ValidationErrors().addError("x"));
        when(generatorFactory.validate(statementFails, database)).thenReturn(new ValidationErrors().addError("y"));

        mockStatic(SqlGeneratorFactory.class);
        when(SqlGeneratorFactory.getInstance()).thenReturn(generatorFactory);

        ValidationErrors errors = new ExampleParamlessAbstractChange() {
            @Override
            public boolean supports(Database database) {
                return true;
            }

            @Override
            public SqlStatement[] generateStatements(Database database) {
                return new SqlStatement[]{statementSkip, statementFails};
            }
        }.validate(database);

        assertEquals(1, errors.getErrorMessages().size());
        assertEquals("y", errors.getErrorMessages().get(0));
    }

    @Test
    public void generateRollbackStatements() throws Exception, RollbackImpossibleException {
        Database database = mock(Database.class);
        final Change change1 = mock(Change.class);
        final Change change2 = mock(Change.class);

        when(change1.supports(database)).thenReturn(true);
        when(change2.supports(database)).thenReturn(true);

        SqlStatement change1Statement1 = mock(SqlStatement.class);
        SqlStatement change1Statement2 = mock(SqlStatement.class);
        SqlStatement change2Statement = mock(SqlStatement.class);

        when(change1.generateStatements(database)).thenReturn(new SqlStatement[] {change1Statement1, change1Statement2});
        when(change2.generateStatements(database)).thenReturn(new SqlStatement[] {change2Statement});

        SqlStatement[] rollbackStatements = new ExampleParamlessAbstractChange() {
            @Override
            protected Change[] createInverses() {
                return new Change[] {change1, change2};
            }
        }.generateRollbackStatements(database);

        assertEquals(3, rollbackStatements.length);
        assertSame(change1Statement1, rollbackStatements[0]);
        assertSame(change1Statement2, rollbackStatements[1]);
        assertSame(change2Statement, rollbackStatements[2]);
    }

    @Test(expected = RollbackImpossibleException.class)
    public void generateRollbackStatements_nullCreateInverse() throws Exception, RollbackImpossibleException {
        Database database = mock(Database.class);
        new ExampleParamlessAbstractChange() {
            @Override
            protected Change[] createInverses() {
                return null;
            }
        }.generateRollbackStatements(database);
    }

    @Test
    public void generateRollbackStatements_emptyCreateInverse() throws Exception, RollbackImpossibleException {
        Database database = mock(Database.class);

        SqlStatement[] rollbackStatements = new ExampleParamlessAbstractChange() {
            @Override
            protected Change[] createInverses() {
                return new Change[0];
            }
        }.generateRollbackStatements(database);

        assertEquals(0, rollbackStatements.length);
    }

    @Test
    public void createInverses() {
        assertNull(new ExampleAbstractChange().createInverses());
    }

    @Test
    public void getResourceAccessor() {
        ResourceAccessor accessor = mock(ResourceAccessor.class);
        ExampleParamlessAbstractChange change = new ExampleParamlessAbstractChange();
        assertNull("resourceAccessor defaults to null", change.getResourceAccessor());

        change.setResourceAccessor(accessor);
        assertSame(accessor, change.getResourceAccessor());

        change.setResourceAccessor(null);
        assertNull("resourceAccessor can be set to null", change.getResourceAccessor());
    }

    @Test
    @PrepareForTest({SqlGeneratorFactory.class})
    public void getAffectedDatabaseObjects() throws Exception {
        Database database = mock(Database.class);
        final SqlStatement statement1 = mock(SqlStatement.class);
        final SqlStatement statement2 = mock(SqlStatement.class);

        SqlGeneratorFactory generatorFactory = mock(SqlGeneratorFactory.class);
        Table table = new Table().setName("x");
        Column column = new Column().setName("y");
        View view = new View().setName("z");
        when(generatorFactory.getAffectedDatabaseObjects(statement1, database)).thenReturn(new HashSet<DatabaseObject>(Arrays.asList(table, column)));
        when(generatorFactory.getAffectedDatabaseObjects(statement2, database)).thenReturn(new HashSet<DatabaseObject>(Arrays.asList(view)));

        mockStatic(SqlGeneratorFactory.class);
        when(SqlGeneratorFactory.getInstance()).thenReturn(generatorFactory);

        Set<DatabaseObject> objects = new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) {
                return new SqlStatement[]{statement1, statement2};
            }
        }.getAffectedDatabaseObjects(database);

        assertEquals(3, objects.size());
        objects.contains(table);
        objects.contains(column);
        objects.contains(view);
    }

    @Test
    public void getAffectedDatabaseObjects_noStatements() throws Exception {
        assertEquals(0, new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) {
                return null;
            }
        }.getAffectedDatabaseObjects(mock(Database.class)).size());

        assertEquals(0, new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) {
                return new SqlStatement[0];
            }
        }.getAffectedDatabaseObjects(mock(Database.class)).size());
    }

    @Test
    public void getSerializableFieldType() {
        assertEquals(LiquibaseSerializable.SerializationType.NAMED_FIELD, new ExampleAbstractChange().getSerializableFieldType("paramOne"));
        assertEquals(LiquibaseSerializable.SerializationType.NESTED_OBJECT, new ExampleAbstractChange().getSerializableFieldType("paramTwo"));
    }

    @Test
    public void supportsRollback() {
        assertTrue(new ExampleParamlessAbstractChange() {
            @Override
            protected Change[] createInverses() {
                return new Change[0];
            }
        }.supportsRollback(mock(Database.class)));

        assertTrue(new ExampleParamlessAbstractChange() {
            @Override
            protected Change[] createInverses() {
                return new Change[] { mock(Change.class)};
            }
        }.supportsRollback(mock(Database.class)));

        assertFalse(new ExampleParamlessAbstractChange() {
            @Override
            protected Change[] createInverses() {
                return null;
            }
        }.supportsRollback(mock(Database.class)));
    }


    @Test
    @PrepareForTest({CheckSum.class})
    public void generateCheckSum() {
        ExampleAbstractChange change = new ExampleAbstractChange();
        String serializedChange = new StringChangeLogSerializer().serialize(change, false);

        mockStatic(CheckSum.class);
        CheckSum checkSum = mock(CheckSum.class);
        when(CheckSum.compute(serializedChange)).thenReturn(checkSum);

        assertSame(checkSum, new ExampleAbstractChange().generateCheckSum());
    }

    @Test
    public void createSinceMetaData() {
        DatabaseChangeProperty property = mock(DatabaseChangeProperty.class);
        when(property.since()).thenReturn("3.4");
        assertEquals("3.4", new ExampleAbstractChange().createSinceMetaData("x", property));
    }

    @Test
    public void createSinceMetaData_nullAnnotation() {
        assertNull(new ExampleAbstractChange().createSinceMetaData("x", null));
    }

    @Test
    public void createDescriptionMetaData() {
        DatabaseChangeProperty property = mock(DatabaseChangeProperty.class);
        when(property.description()).thenReturn("test me");
        assertEquals("test me", new ExampleAbstractChange().createDescriptionMetaData("x", property));
    }

    @Test
    public void createDescriptionMetaData_nullAnnotation() {
        assertNull(new ExampleAbstractChange().createDescriptionMetaData("x", null));
    }

    @Test
    public void createSerializationTypeMetaData() {
        DatabaseChangeProperty property = mock(DatabaseChangeProperty.class);
        when(property.serializationType()).thenReturn(LiquibaseSerializable.SerializationType.NESTED_OBJECT);
        assertEquals(LiquibaseSerializable.SerializationType.NESTED_OBJECT, new ExampleAbstractChange().createSerializationTypeMetaData("x", property));
    }

    @Test
    public void createSerializationTypeMetaData_nullAnnotation() {
        assertEquals(LiquibaseSerializable.SerializationType.NAMED_FIELD, new ExampleAbstractChange().createSerializationTypeMetaData("x", null));
    }

    @Test
    public void createMustEqualExistingMetaData() {
        DatabaseChangeProperty property = mock(DatabaseChangeProperty.class);
        when(property.mustEqualExisting()).thenReturn("table");
        assertEquals("table", new ExampleAbstractChange().createMustEqualExistingMetaData("x", property));
    }

    @Test
    public void createMustEqualExistingMetaData_nullAnnotation() {
        assertNull(new ExampleAbstractChange().createMustEqualExistingMetaData("x", null));
    }

    @Test
    public void createExampleMetaData() {
        DatabaseChangeProperty property = mock(DatabaseChangeProperty.class);
        when(property.exampleValue()).thenReturn("int");
        assertEquals("int", new ExampleAbstractChange().createExampleValueMetaData("x", property));
    }

    @Test
    public void createExampleMetaData_nullAnnotation() {
        assertNull(new ExampleAbstractChange().createExampleValueMetaData("x", null));
    }

    @Test
    public void createRequiredDatabasesMetaData() {
        DatabaseChangeProperty property = mock(DatabaseChangeProperty.class);
        when(property.requiredForDatabase()).thenReturn(new String[] {"a", "b"});
        assertArraysEqual(new String[]{"a", "b"}, new ExampleAbstractChange().createRequiredDatabasesMetaData("x", property));
    }

    @Test
    public void createRequiredDatabasesMetaData_nullAnnotation() {
        assertArraysEqual(new String[]{"COMPUTE"}, new ExampleAbstractChange().createRequiredDatabasesMetaData("x", null));
    }

    @Test
    public void createSupportedDatabasesMetaData() {
        DatabaseChangeProperty property = mock(DatabaseChangeProperty.class);
        when(property.supportsDatabase()).thenReturn(new String[] {"a", "b"});
        assertArraysEqual(new String[]{"a", "b"}, new ExampleAbstractChange().createSupportedDatabasesMetaData("x", property));
    }

    @Test
    public void createSupportedDatabasesMetaData_nullAnnotation() {
        assertArraysEqual(new String[]{"COMPUTE"}, new ExampleAbstractChange().createSupportedDatabasesMetaData("x", null));
    }

    @DatabaseChange(name = "exampleParamelessAbstractChange", description = "Used for the AbstractChangeTest unit test", priority = 1)
    private static class ExampleParamlessAbstractChange extends AbstractChange {

        @Override
        public String getConfirmationMessage() {
            return "Test Confirmation Message";
        }

        @Override
        public SqlStatement[] generateStatements(Database database) {
            return null;
        }

        @Override
        public String getSerializedObjectNamespace() {
            return STANDARD_CHANGELOG_NAMESPACE;
        }

    }
}
