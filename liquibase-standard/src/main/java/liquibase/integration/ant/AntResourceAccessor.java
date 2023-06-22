package liquibase.integration.ant;

import liquibase.Scope;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.DirectoryResourceAccessor;
import liquibase.resource.ZipResourceAccessor;
import liquibase.util.StringUtil;
import org.apache.tools.ant.AntClassLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AntResourceAccessor extends CompositeResourceAccessor {

    public AntResourceAccessor(AntClassLoader classLoader, String changeLogDirectory) {

        if (changeLogDirectory != null) {
            changeLogDirectory = changeLogDirectory.replace("\\", "/");
        }

        if (changeLogDirectory == null) {
            try {
                this.addResourceAccessor(new DirectoryResourceAccessor(Paths.get(".").toAbsolutePath()));
            } catch (FileNotFoundException e) {
                Scope.getCurrentScope().getLog(getClass()).fine(e.getMessage(), e);
            }

            try {
                this.addResourceAccessor(new DirectoryResourceAccessor(Paths.get("/").toAbsolutePath()));
            } catch (FileNotFoundException e) {
                Scope.getCurrentScope().getLog(getClass()).fine(e.getMessage(), e);
            }

        } else {
            try {
                this.addResourceAccessor(new DirectoryResourceAccessor(new File(changeLogDirectory).toPath().toAbsolutePath()));
            } catch (FileNotFoundException e) {
                Scope.getCurrentScope().getLog(getClass()).fine(e.getMessage(), e);
            }

        }

        final String classpath = StringUtil.trimToNull(classLoader.getClasspath());
        if (classpath != null) {
            for (String path : classpath.split(System.getProperty("path.separator"))) {
                try {
                    String lowercasePath = path.toLowerCase();
                    final Path path1 = Paths.get(path);
                    if (lowercasePath.endsWith(".jar") || lowercasePath.endsWith(".zip")) {
                        this.addResourceAccessor(new ZipResourceAccessor(path1.toAbsolutePath()));
                    } else {
                        this.addResourceAccessor(new DirectoryResourceAccessor(path1.toAbsolutePath()));
                    }
                } catch (FileNotFoundException e) {
                    Scope.getCurrentScope().getLog(getClass()).fine(e.getMessage(), e);
                }
            }
        }
    }
}
