package liquibase.change.custom;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.net.URL;
import java.net.URLClassLoader;

import org.junit.Assert;
import org.junit.Test;

import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.SetupException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.SqlStatement;

public class CustomChangeWrapperTest {

    @Test
    public void setClassLoader() {
        URLClassLoader classLoader = new URLClassLoader(new URL[0]);
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper();
        changeWrapper.setClassLoader(classLoader);

        assertSame(classLoader, changeWrapper.getClassLoader());
    }

    @Test
    public void setClass() throws CustomChangeException {
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper();
        changeWrapper.setClassLoader(getClass().getClassLoader());
        changeWrapper.setClass(ExampleCustomSqlChange.class.getName());

        assertTrue(changeWrapper.getCustomChange() instanceof ExampleCustomSqlChange);
        assertEquals(ExampleCustomSqlChange.class.getName(), changeWrapper.getClassName());
    }

//    @Test TODO: Cannot get this test to fire exception code
//    public void setClass_childClassLoader() throws Exception {
//        File testRootDir = new File(getClass().getResource("/"+ExampleCustomSqlChange.class.getName().replace(".","/")+".class").toURI()).getParentFile().getParentFile().getParentFile().getParentFile();
//        File liquibaseRootDir = new File(getClass().getResource("/"+CustomChange.class.getName().replace(".","/")+".class").toURI()).getParentFile().getParentFile().getParentFile().getParentFile();
//
//        ClassLoader childClassLoader = new URLClassLoader(new URL[] {testRootDir.toURI().toURL(), liquibaseRootDir.toURI().toURL()});
//        CustomChangeWrapper changeWrapper = new CustomChangeWrapper();
//        changeWrapper.setClassLoader(childClassLoader);
//
//        changeWrapper.setClass(ExampleCustomSqlChange.class.getName());
//
//        assertTrue(changeWrapper.getCustomChange() instanceof ExampleCustomSqlChange);
//    }

    @Test(expected = CustomChangeException.class)
    public void setClass_classloaderNotSet() throws CustomChangeException {
        new CustomChangeWrapper().setClass(ExampleCustomSqlChange.class.getName());
    }

    @Test
    public void getParams() {
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper();
        assertEquals(0, changeWrapper.getParams().size());

        changeWrapper.setParam("param1", "x");
        changeWrapper.setParam("param2", "y");

        assertEquals(2, changeWrapper.getParams().size());
        assertTrue(changeWrapper.getParams().contains("param1"));
        assertTrue(changeWrapper.getParams().contains("param2"));
    }

    @Test
    public void getParamValues() {
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper();
        assertNull(changeWrapper.getParamValue("notSet"));

        changeWrapper.setParam("param1", "x");
        changeWrapper.setParam("param2", "y");

        assertEquals("x", changeWrapper.getParamValue("param1"));
        assertEquals("y", changeWrapper.getParamValue("param2"));
        assertNull(changeWrapper.getParamValue("badparam"));
    }

    @Test
    public void validate() {
        ValidationErrors errors = new ValidationErrors();
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper();
        Database database = mock(Database.class);
        changeWrapper.customChange = mock(CustomChange.class);
        when(changeWrapper.customChange.validate(database)).thenReturn(errors);

        assertSame(errors, changeWrapper.validate(database));
    }

    @Test
    public void validate_nullReturn() {
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper();
        Database database = mock(Database.class);
        changeWrapper.customChange = mock(CustomChange.class);
        when(changeWrapper.customChange.validate(database)).thenReturn(null);
        assertNull(changeWrapper.validate(database));
    }

    @Test
    public void validate_exceptionInNestedValidate() {
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper();
        Database database = mock(Database.class);
        changeWrapper.customChange = mock(CustomChange.class);
        when(changeWrapper.customChange.validate(database)).thenThrow(NullPointerException.class);
        Assert.assertEquals(1, changeWrapper.validate(database).getErrorMessages().size());
    }

    @Test
    public void warn() {
        assertFalse(new CustomChangeWrapper().warn(mock(Database.class)).hasWarnings());
    }

    @Test
    public void generateStatements_paramsSetCorrectly() throws Exception {
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper();
        changeWrapper.setClassLoader(getClass().getClassLoader());
        changeWrapper.setClass(ExampleCustomSqlChange.class.getName());
        changeWrapper.setParam("name", "myName");
        changeWrapper.setParam("address", "myAddr");

        changeWrapper.generateStatements(mock(Database.class));

        assertEquals("myName", ((ExampleCustomSqlChange) changeWrapper.customChange).name);
        assertEquals("myAddr", ((ExampleCustomSqlChange) changeWrapper.customChange).address);
    }

    @Test(expected = UnexpectedLiquibaseException.class)
    public void generateStatements_paramsSetBad() throws Exception {
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper();
        changeWrapper.setClassLoader(getClass().getClassLoader());
        changeWrapper.setClass(ExampleCustomSqlChange.class.getName());
        changeWrapper.setParam("badParam", "myName");

        changeWrapper.generateStatements(mock(Database.class));
    }

    @Test
    public void generateStatements_sqlStatementsReturned() throws Exception {
        SqlStatement[] statements = new SqlStatement[0];
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper();
        changeWrapper.customChange = mock(CustomSqlChange.class);
        when(((CustomSqlChange) changeWrapper.customChange).generateStatements(any(Database.class))).thenReturn(statements);

        assertSame(statements, changeWrapper.generateStatements(mock(Database.class)));
    }

    @Test
    public void generateStatements_nullSqlStatementsReturned() throws Exception {
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper();
        changeWrapper.customChange = mock(CustomSqlChange.class);
        when(((CustomSqlChange) changeWrapper.customChange).generateStatements(any(Database.class))).thenReturn(null);

        Assert.assertEquals(0, changeWrapper.generateStatements(mock(Database.class)).length);
    }

    @Test
    public void generateStatements_customTask() throws Exception {
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper();
        changeWrapper.customChange = mock(CustomTaskChange.class);

        Assert.assertEquals(0, changeWrapper.generateStatements(mock(Database.class)).length);
        verify(((CustomTaskChange) changeWrapper.customChange)).execute(any(Database.class));
    }

    @Test(expected = UnexpectedLiquibaseException.class)
    public void generateStatements_unknownType() throws Exception {
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper();
        changeWrapper.customChange = mock(CustomChange.class);

        changeWrapper.generateStatements(mock(Database.class));
    }


    @Test
    public void generateRollbackStatements_paramsSetCorrectly() throws Exception {
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper();
        changeWrapper.setClassLoader(getClass().getClassLoader());
        changeWrapper.setClass(ExampleCustomSqlChange.class.getName());
        changeWrapper.setParam("name", "myName");
        changeWrapper.setParam("address", "myAddr");

        changeWrapper.generateRollbackStatements(mock(Database.class));

        assertEquals("myName", ((ExampleCustomSqlChange) changeWrapper.customChange).name);
        assertEquals("myAddr", ((ExampleCustomSqlChange) changeWrapper.customChange).address);
    }

    @Test(expected = UnexpectedLiquibaseException.class)
    public void generateRollbackStatements_paramsSetBad() throws Exception {
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper();
        changeWrapper.setClassLoader(getClass().getClassLoader());
        changeWrapper.setClass(ExampleCustomSqlChange.class.getName());
        changeWrapper.setParam("badParam", "myName");

        changeWrapper.generateRollbackStatements(mock(Database.class));
    }

    @Test
    public void generateRollbackStatements_sqlStatementsReturned() throws Exception {
        SqlStatement[] statements = new SqlStatement[0];
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper();
        changeWrapper.customChange = mock(CustomSqlChange.class, withSettings().extraInterfaces(CustomSqlRollback.class));
        when(((CustomSqlRollback) changeWrapper.customChange).generateRollbackStatements(any(Database.class))).thenReturn(statements);

        assertSame(statements, changeWrapper.generateRollbackStatements(mock(Database.class)));
    }

    @Test
    public void generateRollbackStatements_nullSqlStatementsReturned() throws Exception {
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper();
        changeWrapper.customChange = mock(CustomSqlChange.class, withSettings().extraInterfaces(CustomSqlRollback.class));
        when(((CustomSqlRollback) changeWrapper.customChange).generateRollbackStatements(any(Database.class))).thenReturn(null);

        Assert.assertEquals(0, changeWrapper.generateStatements(mock(Database.class)).length);
    }

    @Test
    public void generateRollbackStatements_customTask() throws Exception {
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper();
        changeWrapper.customChange = mock(CustomTaskChange.class, withSettings().extraInterfaces(CustomTaskRollback.class));

        Assert.assertEquals(0, changeWrapper.generateRollbackStatements(mock(Database.class)).length);
        verify(((CustomTaskRollback) changeWrapper.customChange)).rollback(any(Database.class));
    }

    @Test(expected = RollbackImpossibleException.class)
    public void generateRollbackStatements_unknownType() throws Exception {
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper();
        changeWrapper.customChange = mock(CustomChange.class);

        changeWrapper.generateRollbackStatements(mock(Database.class));
    }

    @Test
    public void supportsRollback() {
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper();

        changeWrapper.customChange = mock(CustomChange.class);
        assertFalse(changeWrapper.supportsRollback(mock(Database.class)));

        changeWrapper.customChange = mock(CustomChange.class, withSettings().extraInterfaces(CustomSqlRollback.class));
        assertTrue(changeWrapper.supportsRollback(mock(Database.class)));

        changeWrapper.customChange = mock(CustomChange.class, withSettings().extraInterfaces(CustomTaskRollback.class));
        assertTrue(changeWrapper.supportsRollback(mock(Database.class)));
    }

    @Test
    public void getConfirmationMessage() {
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper();

        changeWrapper.customChange = mock(CustomChange.class);
        when(changeWrapper.customChange.getConfirmationMessage()).thenReturn("mock message");

        assertEquals("mock message", changeWrapper.getConfirmationMessage());
    }

    public static class ExampleCustomSqlChange implements CustomSqlChange, CustomSqlRollback {

        private String name;
        private String address;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        @Override
        public String getConfirmationMessage() {
            return null;
        }

        @Override
        public void setUp() throws SetupException {
        }

        @Override
        public void setFileOpener(ResourceAccessor resourceAccessor) {
        }

        @Override
        public ValidationErrors validate(Database database) {
            return null;
        }

        @Override
        public SqlStatement[] generateStatements(Database database) throws CustomChangeException {
            return new SqlStatement[0];
        }

        @Override
        public SqlStatement[] generateRollbackStatements(Database database) throws CustomChangeException, RollbackImpossibleException {
            return new SqlStatement[0];
        }
    }
}
