package liquibase.migrator.ant;

import org.apache.tools.ant.Project;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Tests for {@link DatabaseMigratorTask}
 */
public class DatabaseMigratorTaskTest {

    @Test
    public void createClasspath() throws Exception {
        DatabaseMigratorTask databaseMigratorTask = new DatabaseMigratorTask();
        Project project = new Project();
        databaseMigratorTask.setProject(project);

        assertEquals(project, databaseMigratorTask.createClasspath().getProject());
    }
}