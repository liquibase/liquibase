package liquibase.changelog;

import liquibase.ContextExpression;
import liquibase.Contexts;
import liquibase.Labels;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.core.H2Database;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class  ChangeLogParametersTest {

    @Before
    public void before() {
        LiquibaseConfiguration.getInstance().reset();
    }

    @Test
    public void setParameterValue_doubleSet() {
        ChangeLogParameters changeLogParameters = new ChangeLogParameters();

        changeLogParameters.set("doubleSet", "originalValue");
        changeLogParameters.set("doubleSet", "newValue");

        assertEquals("re-setting a param should not overwrite the value (like how ant works)", "originalValue", changeLogParameters.getValue("doubleSet", null));
    }

    @Test
    public void getParameterValue_envVariable() {
        ChangeLogParameters changeLogParameters = new ChangeLogParameters();

        assertEquals(System.getenv("PATH"), changeLogParameters.getValue("PATH", null));
    }

    @Test
    public void getParameterValue_systemProperty() {
        ChangeLogParameters changeLogParameters = new ChangeLogParameters();

        assertEquals(System.getProperty("user.name"), changeLogParameters.getValue("user.name", null));
    }

    @Test
    public void setParameterValue_doubleSetButSecondWrongDatabase() {
        ChangeLogParameters changeLogParameters = new ChangeLogParameters(new H2Database());

        changeLogParameters.set("doubleSet", "originalValue", new ContextExpression(), new Labels(), "baddb", true, null);
        changeLogParameters.set("doubleSet", "newValue");

        assertEquals("newValue", changeLogParameters.getValue("doubleSet", null));
    }

    @Test
    public void setParameterValue_multiDatabase() {
        ChangeLogParameters changeLogParameters = new ChangeLogParameters(new H2Database());

        changeLogParameters.set("doubleSet", "originalValue", new ContextExpression(), new Labels(), "baddb, h2", true, null);

        assertEquals("originalValue", changeLogParameters.getValue("doubleSet", null));
    }

    @Test
    public void setParameterValue_rightDBWrongContext() {
        ChangeLogParameters changeLogParameters = new ChangeLogParameters(new H2Database());
        changeLogParameters.setContexts(new Contexts("junit"));

        changeLogParameters.set("doubleSet", "originalValue", "anotherContext", "anotherLabel", "baddb, h2", true, null);

        assertNull(changeLogParameters.getValue("doubleSet", null));
    }
   @Test
    public void setParameterValue_rightDBRightContext() {
        ChangeLogParameters changeLogParameters = new ChangeLogParameters(new H2Database());
        changeLogParameters.setContexts(new Contexts("junit"));

        changeLogParameters.set("doubleSet", "originalValue", "junit", "junitLabel", "baddb, h2", true, null);

        assertEquals("originalValue", changeLogParameters.getValue("doubleSet", null));
    }

    @Test
    /**
     * root.xml
     *  -a.xml
     *  -b.xml
     *
     *  in a and b we define same prop with key 'aKey'. Expected when b is processed then bValue is taken no matter of Object instances
     */
    public void getParameterValue_SamePropertyNonGlobalIn2InnerFiles() {
        DatabaseChangeLog inner1 = new DatabaseChangeLog();
        inner1.setPhysicalFilePath("a");
        DatabaseChangeLog inner2 = new DatabaseChangeLog();
        inner2.setPhysicalFilePath("b");
        ChangeLogParameters changeLogParameters = new ChangeLogParameters(new H2Database());
        changeLogParameters.set("aKey", "aValue", "junit", "junitLabel", "baddb, h2", false, inner1);
        changeLogParameters.set("aKey", "bValue", "junit", "junitLabel", "baddb, h2", false, inner2);
        DatabaseChangeLog inner2SamePath = new DatabaseChangeLog();
        inner2SamePath.setPhysicalFilePath("b");
        Object aKey = changeLogParameters.getValue("aKey", inner2SamePath);
        assertEquals("bValue", aKey);
    }

    @Test
    /**
     * db.changelog-master.xml
     * - table_1.xml (table.name=table_1 global=false)
     * - - include templates/common_columns_1.xml
     * - table_2.xml (table.name=table_2 global=false)
     * - - include templates/common_columns_2.xml
     * - - - include templates/common_columns_1.xml
     *
     *  For local parameters, return the value of the definition in the closest direct ancestor of the requesting changeSet.
     */
    public void getParameterValue_ReturnValueOfLocalParameterDefinedInTheClosestAncestorChangeSet() {
        ChangeLogParameters changeLogParameters = new ChangeLogParameters(new H2Database());
        DatabaseChangeLog master = new DatabaseChangeLog("db/db.changelog-master.xml");

        DatabaseChangeLog table_1 = new DatabaseChangeLog("db/changelog/table_1.xml");
        table_1.setParentChangeLog(master);
        changeLogParameters.set("table.name", "table_1", "junit", "junitLabel", "baddb, h2", false, table_1);
        DatabaseChangeLog common_columns_1_of_table_1 = new DatabaseChangeLog("db/templates/common_columns_1.xml");
        common_columns_1_of_table_1.setParentChangeLog(table_1);

        assertEquals("table_1", changeLogParameters.getValue("table.name", common_columns_1_of_table_1));
        assertEquals("table_1", changeLogParameters.getValue("table.name", table_1));

        DatabaseChangeLog table_2 = new DatabaseChangeLog("db/changelog/table_2.xml");
        table_2.setParentChangeLog(master);
        changeLogParameters.set("table.name", "table_2", "junit", "junitLabel", "baddb, h2", false, table_2);
        DatabaseChangeLog common_columns_2_of_table_2 = new DatabaseChangeLog("db/templates/common_columns_2.xml");
        common_columns_2_of_table_2.setParentChangeLog(table_2);
        DatabaseChangeLog common_columns_1_of_table_2 = new DatabaseChangeLog("db/templates/common_columns_1.xml");
        common_columns_1_of_table_2.setParentChangeLog(common_columns_2_of_table_2);

        assertEquals("should return local value of closest ancestor changeSet (here the grand parent)", "table_2", changeLogParameters.getValue("table.name", common_columns_1_of_table_2));
        assertEquals("should return local value of closest ancestor changeSet (here the direct parent)", "table_2", changeLogParameters.getValue("table.name", common_columns_2_of_table_2));
        assertEquals("should return local value of changeSet", "table_2", changeLogParameters.getValue("table.name", table_2));
    }

    @Test
    /**
     * db.changelog-master.xml
     * - table_1.xml (table.name=table_1 global=false)
     * - - include templates/common_columns_1.xml
     * - table_2.xml (table.name=table_2 global=false)
     * - - include templates/common_columns_2.xml
     * - - - include templates/common_columns_1.xml
     *
     *  Return <code>null</code> if there is a local parameter, but its not defined in an direct ancestor changeSet.
     */
    public void getParameterValue_ReturnNullIfLocalParameterNotInAncestorChangeSet() {
        ChangeLogParameters changeLogParameters = new ChangeLogParameters(new H2Database());
        DatabaseChangeLog master = new DatabaseChangeLog("db/db.changelog-master.xml");

        DatabaseChangeLog table_1 = new DatabaseChangeLog("db/changelog/table_1.xml");
        table_1.setParentChangeLog(master);
        changeLogParameters.set("table.name", "table_1", "junit", "junitLabel", "baddb, h2", false, table_1);
        DatabaseChangeLog common_columns_1_of_table_1 = new DatabaseChangeLog("db/templates/common_columns_1.xml");
        common_columns_1_of_table_1.setParentChangeLog(table_1);

        assertEquals("table_1", changeLogParameters.getValue("table.name", common_columns_1_of_table_1));
        assertEquals("table_1", changeLogParameters.getValue("table.name", table_1));

        DatabaseChangeLog table_2 = new DatabaseChangeLog("db/changelog/table_2.xml");
        table_2.setParentChangeLog(master);
        // programmer forgot to define the property "table.name" in this changeSet
        // changeLogParameters.set("table.name", "table_2", "junit", "junitLabel", "baddb, h2", false, table_2);
        DatabaseChangeLog common_columns_2_of_table_2 = new DatabaseChangeLog("db/templates/common_columns_2.xml");
        common_columns_2_of_table_2.setParentChangeLog(table_2);
        DatabaseChangeLog common_columns_1_of_table_2 = new DatabaseChangeLog("db/templates/common_columns_1.xml");
        common_columns_1_of_table_2.setParentChangeLog(common_columns_2_of_table_2);

        // even though there is a single value for "table.name" it is not being used since it does not belong to an ancestor of the requesting changeSet
        assertNull("should return no value since there is no matching global key and no local key belonging to the current changeSet or an ancestor changeSet", changeLogParameters.getValue("table.name", common_columns_1_of_table_2));
        assertNull("should return no value since there is no matching global key and no local key belonging to the current changeSet or an ancestor changeSet", changeLogParameters.getValue("table.name", common_columns_2_of_table_2));
        assertNull("should return no value since there is no matching global key and no local key belonging to the current changeSet or an ancestor changeSet", changeLogParameters.getValue("table.name", table_2));
    }

    @Test
    /**
     * db.changelog-master.xml
     * - table_1.xml (table.name=table_1 global=false)
     * - - include templates/common_columns_1.xml
     * - table_2.xml (table.name=table_2 global=false)
     * - - include templates/common_columns_2.xml
     * - - - include templates/common_columns_1.xml
     *
     *  Local parameters with the same name are ignore after the first global parameter with that same name.
     */
    public void getParameterValue_IgnoreLocalParameterAfterGlobalParameter() {
        ChangeLogParameters changeLogParameters = new ChangeLogParameters(new H2Database());
        DatabaseChangeLog master = new DatabaseChangeLog("db/db.changelog-master.xml");

        DatabaseChangeLog table_1 = new DatabaseChangeLog("db/changelog/table_1.xml");
        table_1.setParentChangeLog(master);
        changeLogParameters.set("table.name", "table_1", "junit", "junitLabel", "baddb, h2", true, table_1);
        DatabaseChangeLog common_columns_1_of_table_1 = new DatabaseChangeLog("db/templates/common_columns_1.xml");
        common_columns_1_of_table_1.setParentChangeLog(table_1);

        assertEquals("table_1", changeLogParameters.getValue("table.name", common_columns_1_of_table_1));
        assertEquals("table_1", changeLogParameters.getValue("table.name", table_1));

        DatabaseChangeLog table_2 = new DatabaseChangeLog("db/changelog/table_2.xml");
        table_2.setParentChangeLog(master);
        changeLogParameters.set("table.name", "table_2", "junit", "junitLabel", "baddb, h2", false, table_2);
        DatabaseChangeLog common_columns_2_of_table_2 = new DatabaseChangeLog("db/templates/common_columns_2.xml");
        common_columns_2_of_table_2.setParentChangeLog(table_2);
        DatabaseChangeLog common_columns_1_of_table_2 = new DatabaseChangeLog("db/templates/common_columns_1.xml");
        common_columns_1_of_table_2.setParentChangeLog(common_columns_2_of_table_2);

        // the local parameter value "table_2" is being ignored since there is already a global value defined
        assertEquals("should return first global value", "table_1", changeLogParameters.getValue("table.name", common_columns_1_of_table_2));
        assertEquals("should return first global value", "table_1", changeLogParameters.getValue("table.name", common_columns_2_of_table_2));
        assertEquals("should return first global value", "table_1", changeLogParameters.getValue("table.name", table_2));
    }

    @Test
    /**
     * master.xml
     * - table_1.xml (table.name=table_1 global=false)
     * - - include_of_table_1.xml
     * - - - include_of_include_of_table_1.xml
     *
     *  The same parameter defined multiple times on different levels of included files
     */
    public void getParameterValue_MultipleLocalParametersInOneHierarchy() {
        ChangeLogParameters changeLogParameters = new ChangeLogParameters(new H2Database());
        DatabaseChangeLog master = new DatabaseChangeLog("master.xml");

        DatabaseChangeLog table_1 = new DatabaseChangeLog("table_1.xml");
        table_1.setParentChangeLog(master);
        changeLogParameters.set("aKey", "aValue", "junit", "junitLabel", "baddb, h2", false, table_1);
        DatabaseChangeLog include_of_table_1 = new DatabaseChangeLog("include_of_table_1.xml");
        include_of_table_1.setParentChangeLog(table_1);
        DatabaseChangeLog include_of_include_of_table_1 = new DatabaseChangeLog("include_of_include_of_table_1.xml");
        include_of_include_of_table_1.setParentChangeLog(include_of_table_1);
        changeLogParameters.set("aKey", "bValue", "junit", "junitLabel", "baddb, h2", false, include_of_include_of_table_1);

        assertEquals("should return local value of changeSet", "bValue", changeLogParameters.getValue("aKey", include_of_include_of_table_1));
        assertEquals("should return local value of closest ancestor changeSet (here the direct parent)", "aValue", changeLogParameters.getValue("aKey", include_of_table_1));
        assertEquals("should return local value of changeSet", "aValue", changeLogParameters.getValue("aKey", table_1));
    }
}
