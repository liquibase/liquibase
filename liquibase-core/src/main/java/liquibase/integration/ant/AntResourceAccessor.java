package liquibase.integration.ant;

import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.DirectoryResourceAccessor;
import liquibase.resource.ZipResourceAccessor;
import liquibase.util.StringUtil;
import org.apache.tools.ant.AntClassLoader;

import java.io.File;
import java.nio.file.Paths;

public class AntResourceAccessor extends CompositeResourceAccessor {

    public AntResourceAccessor(AntClassLoader classLoader, String changeLogDirectory) {

        if (changeLogDirectory != null) {
            changeLogDirectory = changeLogDirectory.replace("\\", "/");
        }

        if (changeLogDirectory == null) {
            this.addResourceAccessor(new DirectoryResourceAccessor(Paths.get(".").toAbsolutePath()));
            this.addResourceAccessor(new DirectoryResourceAccessor(Paths.get("/").toAbsolutePath()));
        } else {
            this.addResourceAccessor(new DirectoryResourceAccessor(new File(changeLogDirectory).toPath().toAbsolutePath()));
        }

        final String classpath = StringUtil.trimToNull(classLoader.getClasspath());
        if (classpath != null) {
            for (String path : classpath.split(System.getProperty("path.separator"))) {
                String lowercasePath = path.toLowerCase();
                if (lowercasePath.endsWith(".jar") || lowercasePath.endsWith(".zip")) {
                    this.addResourceAccessor(new ZipResourceAccessor(Paths.get(path).toAbsolutePath()));
                } else {
                    this.addResourceAccessor(new DirectoryResourceAccessor(Paths.get(path).toAbsolutePath()));
                }
            }
        }
    }
}
