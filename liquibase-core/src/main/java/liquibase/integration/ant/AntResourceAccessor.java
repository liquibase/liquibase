package liquibase.integration.ant;

import liquibase.resource.FileSystemResourceAccessor;
import org.apache.tools.ant.AntClassLoader;

import java.io.File;

public class AntResourceAccessor extends FileSystemResourceAccessor {

    public AntResourceAccessor(AntClassLoader classLoader, String changeLogDirectory) {

        if (changeLogDirectory != null) {
            this.addRootPath(new File(changeLogDirectory).getAbsoluteFile().toPath());
        }

        for (String path : classLoader.getClasspath().split(System.getProperty("path.separator"))) {
            this.addRootPath(new File(path).toPath());
        }


    }

}
