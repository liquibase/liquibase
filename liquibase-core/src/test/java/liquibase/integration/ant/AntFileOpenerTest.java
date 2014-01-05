package liquibase.integration.ant;

import liquibase.resource.AbstractFileOpenerTest;
import liquibase.resource.ResourceAccessor;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import static org.junit.Assert.*;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * Tests for {@link liquibase.integration.ant.AntResourceAccessor}
 */
public class AntFileOpenerTest extends AbstractFileOpenerTest {

    /**
     * @see liquibase.resource.AbstractFileOpenerTest#createFileOpener()
     */
    @Override
    protected ResourceAccessor createFileOpener() {
        Project project = new Project();
        return new AntResourceAccessor(project, new Path(project));
    }

    @Test
    public void getResourceAsStream() throws Exception {
      InputStream inputStream = resourceAccessor.getResourceAsStream("liquibase/integration/ant/AntFileOpenerTest.class");
      assertNotNull(inputStream);
    }

    @Test
    public void getResourceAsStreamNonExistantFile() throws Exception {
      assertNull(resourceAccessor.getResourceAsStream("non/existant/file.txt"));
    }

    @Test
    public void getResources() throws Exception {
      Enumeration<URL> resources = resourceAccessor.getResources("liquibase/ant");
      assertNotNull(resources);
    }
}
