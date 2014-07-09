package liquibase.integration.ant;

import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * An implementation of FileOpener that is specific to how Ant works.
 */
public class AntResourceAccessor extends CompositeResourceAccessor {

    public AntResourceAccessor(final Project project, final Path classpath) {
        super(new ClassLoaderResourceAccessor(
                AccessController.doPrivileged(new PrivilegedAction<AntClassLoader>() {
                    @Override
                    public AntClassLoader run() {
                        return new AntClassLoader(project, classpath);
                    }
                })),
                new ClassLoaderResourceAccessor(
                        AccessController.doPrivileged(new PrivilegedAction<AntClassLoader>() {
                            @Override
                            public AntClassLoader run() {
                                return new AntClassLoader(project, new Path(project, "."));
                            }
                        }))
        );
    }
}

