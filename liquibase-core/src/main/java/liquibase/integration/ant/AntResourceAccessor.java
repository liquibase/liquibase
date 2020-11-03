package liquibase.integration.ant;

import liquibase.resource.FileSystemResourceAccessor;
import liquibase.util.StringUtil;
import org.apache.tools.ant.AntClassLoader;

import java.io.File;

public class AntResourceAccessor extends FileSystemResourceAccessor {

    public AntResourceAccessor(AntClassLoader classLoader, String changeLogDirectory) {

        if (changeLogDirectory != null) {
            changeLogDirectory = changeLogDirectory.replace("\\", "/");
        }

        if (changeLogDirectory == null) {
            this.addRootPath(new File("").getAbsoluteFile().toPath());
            this.addRootPath(new File("/").getAbsoluteFile().toPath());
        } else {
            this.addRootPath(new File(changeLogDirectory).getAbsoluteFile().toPath());
        }

        final String classpath = StringUtil.trimToNull(classLoader.getClasspath());
        if (classpath != null) {
            for (String path : classpath.split(System.getProperty("path.separator"))) {
                this.addRootPath(new File(path).toPath());
            }
        }
    }

}
