package liquibase.migrator.ant;

import junit.framework.*;
import liquibase.migrator.ant.AntFileOpener;
import liquibase.migrator.AbstractFileOpenerTest;
import liquibase.migrator.FileOpener;
import liquibase.migrator.commandline.CommandLineFileOpener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

public class AntFileOpenerTest  extends AbstractFileOpenerTest {

    protected FileOpener createFileOpener() {
        Project project = new Project();
        return new AntFileOpener(project, new Path(project));
    }
}