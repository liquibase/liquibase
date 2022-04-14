package liquibase.resource;

import liquibase.Scope;
import liquibase.integration.commandline.CommandLineResourceAccessor;
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;
import liquibase.servicelocator.PrioritizedService;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

import static liquibase.util.SystemUtil.isWindows;

public class StandardResourceAccessorService implements ResourceAccessorService {
    @Override
    public ResourceAccessor getResourceAccessor(ClassLoader classLoader) {
        return new CompositeResourceAccessor(new FileSystemResourceAccessor(Paths.get(".").toAbsolutePath().toFile()),
                                             new CommandLineResourceAccessor(classLoader));
    }

    @Override
    public int getPriority() {
        return PrioritizedService.PRIORITY_DEFAULT;
    }
}
