package liquibase.migrator.ant;

import liquibase.migrator.AbstractFileOpenerTest;
import liquibase.migrator.FileOpener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;

public class AntFileOpenerTest extends AbstractFileOpenerTest {

    protected FileOpener createFileOpener() {
        Project project = new Project();
        return new AntFileOpener(project, new Path(project));
    }
}