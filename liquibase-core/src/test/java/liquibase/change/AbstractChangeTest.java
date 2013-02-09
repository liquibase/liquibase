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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SqlGeneratorFactory.class, CheckSum.class})
public class AbstractChangeTest {

    @Test
    public void constructor() {
        final ChangeMetaData metaData = mock(ChangeMetaData.class);
        AbstractChange change = new ExampleAbstractChange() {
            @Override
            protected ChangeMetaData createChangeMetaData() {
                return metaData;
            }
        };
        assertSame(metaData, change.getChangeMetaData());
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
                public String getConfirmationMessage() {
                    return null;
                }

                public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
                    return null;
                }
            };
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
        ChangeParameterMetaData paramOneMetaData = parameters.get("paramOne");
        ChangeParameterMetaData paramTwoMetaData = parameters.get("paramTwo");
        ChangeParameterMetaData paramNoMetaData = parameters.get("paramNoMetadata");
        ChangeParameterMetaData paramNotIncludedMetaData = parameters.get("paramNotIncluded");
        ChangeParameterMetaData paramNoWriteMethodMetaData = parameters.get("paramNoWriteMethod");

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
    public void generateStatementsVolatile() throws UnsupportedChangeException {
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
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException { return new SqlStatement[] { statement1Yes, statement2Yes }; }
        }.generateStatementsVolatile(database));

        assertFalse(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException { return new SqlStatement[]{statement1No, statement2No}; }
        }.generateStatementsVolatile(database));

        assertTrue(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
                return new SqlStatement[]{statement1No, statement1Yes};
            }
        }.generateStatementsVolatile(database));
    }

    @Test
    public void generateStatementsVolatile_noStatements() throws UnsupportedChangeException {
        assertFalse(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
                return null;
            }
        }.generateStatementsVolatile(mock(Database.class)));

        assertFalse(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
                return new SqlStatement[0];
            }
        }.generateStatementsVolatile(mock(Database.class)));
    }

    @Test
    public void generateStatementsVolatile_unsupportedChangeException() throws UnsupportedChangeException {
        assertFalse(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
                throw new UnsupportedChangeException("test exception");
            }
        }.generateStatementsVolatile(mock(Database.class)));
    }

    @Test
    public void generateRollbackStatementsVolatile() throws UnsupportedChangeException {
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
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException { return new SqlStatement[] { statement1Yes, statement2Yes }; }
        }.generateRollbackStatementsVolatile(database));

        assertFalse(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException { return new SqlStatement[]{statement1No, statement2No}; }
        }.generateRollbackStatementsVolatile(database));

        assertTrue(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException { return new SqlStatement[]{statement1No, statement1Yes}; }
        }.generateRollbackStatementsVolatile(database));
    }

    @Test
    public void generateRollbackStatementsVolatile_noStatements() throws UnsupportedChangeException {
        assertFalse(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
                return null;
            }
        }.generateRollbackStatementsVolatile(mock(Database.class)));

        assertFalse(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
                return new SqlStatement[0];
            }
        }.generateRollbackStatementsVolatile(mock(Database.class)));
    }
    @Test
    public void generateRollbackStatementsVolatile_unsupportedChangeException() throws UnsupportedChangeException {
        assertFalse(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
                throw new UnsupportedChangeException("test exception");
            }
        }.generateRollbackStatementsVolatile(mock(Database.class)));
    }


    @Test
    public void supports() throws UnsupportedChangeException {
        Database database = mock(Database.class);
        final SqlStatement statement1No = mock(SqlStatement.class);
        final SqlStatement statement2No = mock(SqlStatement.class);
        final SqlStatement statement1Yes = mock(SqlStatement.class);
        final SqlStatement statement2Yes = mock(SqlStatement.class);

        SqlGeneratorFactory generatorFactory = mock(SqlGeneratorFactory.class);
        when(generatorFactory.supports(statement1No, database)).thenReturn(false);
        when(generatorFactory.supports(statement2No, database)).thenReturn(false);
        when(generatorFactory.supports(statement1Yes, database)).thenReturn(true);
        when(generatorFactory.supports(statement2Yes, database)).thenReturn(true);

        mockStatic(SqlGeneratorFactory.class);
        when(SqlGeneratorFactory.getInstance()).thenReturn(generatorFactory);

        assertTrue(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException { return new SqlStatement[] { statement1Yes, statement2Yes }; }
        }.supports(database));

        assertFalse(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException { return new SqlStatement[]{statement1No, statement2No}; }
        }.supports(database));

        assertFalse(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
                return new SqlStatement[]{statement1No, statement1Yes};
            }
        }.supports(database));
    }

    @Test
    public void supports_generateThrowsException() throws UnsupportedChangeException {
        Database database = mock(Database.class);

        assertFalse(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
                throw new UnsupportedChangeException("Not Supported Test");
            }
        }.supports(database));

    }

    @Test
    public void supports_noStatements() throws UnsupportedChangeException {
        assertTrue(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
                return null;
            }
        }.supports(mock(Database.class)));

        assertTrue(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
                return new SqlStatement[0];
            }
        }.supports(mock(Database.class)));
    }

    @Test
    public void warn() throws UnsupportedChangeException {
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
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
                return new SqlStatement[]{statementUnsupported, statementNullWarnings, statementEmptyWarnings, statementOneWarning, statementTwoWarnings};
            }
        }.warn(database);

        assertEquals(3, warningsToTest.getMessages().size());
        assertEquals("x", warningsToTest.getMessages().get(0));
        assertEquals("y", warningsToTest.getMessages().get(1));
        assertEquals("z", warningsToTest.getMessages().get(2));
    }

    @Test
    public void warn_unsupportedButSkip() throws UnsupportedChangeException {
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
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
                return new SqlStatement[]{statementSkip, statementFails};
            }
        }.warn(database);

        assertEquals(2, warnings.getMessages().size());
        assertTrue(warnings.getMessages().get(0), warnings.getMessages().get(0).matches("liquibase.statement.SqlStatement.* is not supported on mysql, but exampleParamelessAbstractChange will still execute"));
        assertEquals("y", warnings.getMessages().get(1));
    }

    @Test
    public void warn_noStatements() throws UnsupportedChangeException {
        assertFalse(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
                return null;
            }
        }.warn(mock(Database.class)).hasWarnings());

        assertFalse(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
                return new SqlStatement[0];
            }
        }.warn(mock(Database.class)).hasWarnings());
    }

    @Test
    public void warn_unsupportedChangeException() throws UnsupportedChangeException {
        assertFalse(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
                throw new UnsupportedChangeException("test exception");
            }
        }.warn(mock(Database.class)).hasWarnings());
    }

    @Test
    public void validate_noStatements() throws UnsupportedChangeException {
        assertFalse(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
                return null;
            }
        }.validate(mock(Database.class)).hasErrors());

        assertFalse(new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
                return new SqlStatement[0];
            }
        }.validate(mock(Database.class)).hasErrors());
    }

    @Test
    public void validate_unsupportedChangeException() throws UnsupportedChangeException {
        ValidationErrors errors = new ExampleParamlessAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
                throw new UnsupportedChangeException("test exception");
            }
        }.validate(new MySQLDatabase());
        assertEquals(1, errors.getErrorMessages().size());
        assertEquals("exampleParamelessAbstractChange is not supported on mysql", errors.getErrorMessages().get(0));
    }

    @Test
    public void validate_noParameters() throws UnsupportedChangeException {
        assertFalse(new ExampleParamlessAbstractChange().validate(mock(Database.class)).hasErrors());
    }

    @Test
    public void validate_notSupported() throws UnsupportedChangeException {
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
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
                return new SqlStatement[]{statement1Unsupported, statementSupported, statement2Unsupported};
            }
        }.validate(database);

        assertEquals("Should only return one meessage, even through two statements failed", 1, errors.getErrorMessages().size());
        assertEquals("exampleParamelessAbstractChange is not supported on mysql", errors.getErrorMessages().get(0));
    }

    @Test
    public void validate_statementsHaveErrors() throws UnsupportedChangeException {
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
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
                return new SqlStatement[]{statement1Fails, statement2Fails, statementNoErrors};
            }
        }.validate(database);

        assertEquals(3, errors.getErrorMessages().size());
        assertEquals("x", errors.getErrorMessages().get(0));
        assertEquals("y", errors.getErrorMessages().get(1));
        assertEquals("z", errors.getErrorMessages().get(2));
    }

    @Test
    public void validate_missingRequiredValue() throws UnsupportedChangeException {
        ExampleAbstractChange change = new ExampleAbstractChange();
        ValidationErrors errors = change.validate(new MSSQLDatabase());
        assertEquals(1, errors.getErrorMessages().size());
        assertEquals("paramTwo is required for exampleAbstractChange on mssql", errors.getErrorMessages().get(0));
    }

    @Test
    public void validate_noErrors() throws UnsupportedChangeException {
        ExampleAbstractChange change = new ExampleAbstractChange();
        change.setParamTwo(3);
        ValidationErrors errors = change.validate(new MSSQLDatabase());
        assertFalse(errors.hasErrors());
    }

    @Test
    public void validate_noErrorsForSkipOnUnsupported() throws UnsupportedChangeException {
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
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
                return new SqlStatement[]{statementSkip, statementFails};
            }
        }.validate(database);

        assertEquals(1, errors.getErrorMessages().size());
        assertEquals("y", errors.getErrorMessages().get(0));
    }

    @Test
    public void generateRollbackStatements() throws UnsupportedChangeException, RollbackImpossibleException {
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
    public void generateRollbackStatements_nullCreateInverse() throws UnsupportedChangeException, RollbackImpossibleException {
        Database database = mock(Database.class);
        new ExampleParamlessAbstractChange() {
            @Override
            protected Change[] createInverses() {
                return null;
            }
        }.generateRollbackStatements(database);
    }

    @Test
    public void generateRollbackStatements_emptyCreateInverse() throws UnsupportedChangeException, RollbackImpossibleException {
        Database database = mock(Database.class);

        SqlStatement[] rollbackStatements = new ExampleParamlessAbstractChange() {
            @Override
            protected Change[] createInverses() {
                return new Change[0];
            }
        }.generateRollbackStatements(database);

        assertEquals(0, rollbackStatements.length);
    }

    @Test(expected = RollbackImpossibleException.class)
    public void generateRollbackStatements_unsupportedChangeException() throws RollbackImpossibleException {
        new ExampleAbstractChange() {
            @Override
            protected Change[] createInverses()  {
                try {
                    Change change1 = mock(Change.class);
                    when(change1.supports(any(Database.class))).thenReturn(true);
                    when(change1.getChangeMetaData()).thenReturn(new ChangeMetaData("testChange", null, 1, null, null));
                    when(change1.generateStatements(any(Database.class))).thenThrow(new UnsupportedChangeException("testing exception"));
                    return new Change[] {
                            change1
                    };
                } catch (UnsupportedChangeException e) {
                    throw new RuntimeException(e);
                }
            }
        }.generateRollbackStatements(mock(Database.class));
    }

    @Test(expected = RollbackImpossibleException.class)
    public void generateRollbackStatements_notSupportedChange() throws RollbackImpossibleException {
        new ExampleAbstractChange() {
            @Override
            protected Change[] createInverses()  {
                Change change1 = mock(Change.class);
                when(change1.getChangeMetaData()).thenReturn(new ChangeMetaData("testChange", null, 1, null, null));
                when(change1.supports(any(Database.class))).thenReturn(false);
                return new Change[] {
                        change1
                };
            }
        }.generateRollbackStatements(mock(Database.class));
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
    public void getAffectedDatabaseObjects() throws UnsupportedChangeException {
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
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
                return new SqlStatement[]{statement1, statement2};
            }
        }.getAffectedDatabaseObjects(database);

        assertEquals(3, objects.size());
        objects.contains(table);
        objects.contains(column);
        objects.contains(view);
    }

    @Test
    public void getAffectedDatabaseObjects_noStatements() throws UnsupportedChangeException {
        assertEquals(0, new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
                return null;
            }
        }.getAffectedDatabaseObjects(mock(Database.class)).size());

        assertEquals(0, new ExampleAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
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
    public void getAffectedDatabaseObjects_unsupportedChangeException() throws UnsupportedChangeException {
        assertEquals(0, new ExampleParamlessAbstractChange() {
            @Override
            public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
                throw new UnsupportedChangeException("test exception");
            }
        }.getAffectedDatabaseObjects(new MySQLDatabase()).size());
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
    public void generateCheckSum() {
        ExampleAbstractChange change = new ExampleAbstractChange();
        String serializedChange = new StringChangeLogSerializer().serialize(change);

        mockStatic(CheckSum.class);
        CheckSum checkSum = mock(CheckSum.class);
        when(CheckSum.compute(serializedChange)).thenReturn(checkSum);

        assertSame(checkSum, new ExampleAbstractChange().generateCheckSum());
    }


    @DatabaseChange(name = "exampleAbstractChange", description = "Used for the AbstractChangeTest unit test", priority = 1)
    private static class ExampleAbstractChange extends AbstractChange {

        private String paramOne;
        private Integer paramTwo;
        private String paramNoMetadata;
        private String paramNotIncluded;

        public String getConfirmationMessage() {
            return "Test Confirmation Message";
        }

        public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
            return null;
        }

        @DatabaseChangeProperty
        public String getParamOne() {
            return paramOne;
        }

        public void setParamOne(String paramOne) {
            this.paramOne = paramOne;
        }

        @DatabaseChangeProperty(requiredForDatabase = {"mysql", "mssql"}, mustEqualExisting = "table", serializationType = SerializationType.NESTED_OBJECT)
        public Integer getParamTwo() {
            return paramTwo;
        }

        public void setParamTwo(Integer paramTwo) {
            this.paramTwo = paramTwo;
        }

        public String getParamNoMetadata() {
            return paramNoMetadata;
        }

        public void setParamNoMetadata(String paramNoMetadata) {
            this.paramNoMetadata = paramNoMetadata;
        }

        @DatabaseChangeProperty(isChangeProperty = false)
        public String getParamNotIncluded() {
            return paramNotIncluded;
        }

        public void setParamNotIncluded(String paramNotIncluded) {
            this.paramNotIncluded = paramNotIncluded;
        }

        public String getNotWriteMethod() {
            return null;
        }
    }

    @DatabaseChange(name = "exampleParamelessAbstractChange", description = "Used for the AbstractChangeTest unit test", priority = 1)
    private static class ExampleParamlessAbstractChange extends AbstractChange {

        public String getConfirmationMessage() {
            return "Test Confirmation Message";
        }

        public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
            return null;
        }
    }
}
