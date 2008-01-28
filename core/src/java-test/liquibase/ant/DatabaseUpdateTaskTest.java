package liquibase.ant;

import org.apache.tools.ant.Project;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests for {@link DatabaseUpdateTask}
 */
public class DatabaseUpdateTaskTest {

    @Test
    public void createClasspath() throws Exception {
        DatabaseUpdateTask databaseUpdateTask = new DatabaseUpdateTask();
        Project project = new Project();
        databaseUpdateTask.setProject(project);

        assertEquals(project, databaseUpdateTask.createClasspath().getProject());
    }
}