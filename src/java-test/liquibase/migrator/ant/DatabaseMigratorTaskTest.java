package liquibase.migrator.ant;

import junit.framework.TestCase;
import org.apache.tools.ant.Project;
import liquibase.migrator.ant.DatabaseMigratorTask;

public class DatabaseMigratorTaskTest extends TestCase {

    public void testCreateClasspath() throws Exception {
        DatabaseMigratorTask databaseMigratorTask = new DatabaseMigratorTask();
        Project project = new Project();
        databaseMigratorTask.setProject(project);

        assertEquals(project, databaseMigratorTask.createClasspath().getProject());
    }
}