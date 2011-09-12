package liquibase.integration.ant;

import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;

/**
 * An implementation of FileOpener that is specific to how Ant works.
 */
public class AntResourceAccessor extends CompositeResourceAccessor {

    public AntResourceAccessor(final Project project, final Path classpath) {
        super(new ClassLoaderResourceAccessor(
                AccessController.doPrivileged(new PrivilegedAction<AntClassLoader>() {
                    public AntClassLoader run() {
                        return new AntClassLoader(project, classpath);
                    }
                })),
                new ClassLoaderResourceAccessor(
                        AccessController.doPrivileged(new PrivilegedAction<AntClassLoader>() {
                            public AntClassLoader run() {
                                return new AntClassLoader(project, new Path(project, "."));
                            }
                        }))
        );
    }
}

