package liquibase.extension.testing.environment.core;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.extension.testing.environment.TestEnvironment;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TestcontainerEnvironment extends TestEnvironment {

    private final GenericContainer container;

    public TestcontainerEnvironment(String env) {
        super(env);

        Class containerClass = getProperty(env, "containerClass", Class.class, true);
        String imageName = getProperty(env, "imageName", String.class, true);
        String imageVersion = getProperty(env, "imageVersion", String.class, true);

        try {
            container = (GenericContainer) containerClass.newInstance();
        } catch (Throwable e) {
            throw new UnexpectedLiquibaseException("Cannot instantiate "+containerClass.getName()+" for "+env+": "+e.getMessage(), e);
        }

        container.setDockerImageName(imageName + ":" + imageVersion);

        int[] ports = getProperty(env, "ports", value -> {
            if (value == null) {
                return null;
            }
            final String[] portStrings = String.valueOf(value).split("\\s*,\\s*");

            int[] returnValue = new int[portStrings.length];
            for (int i = 0; i < portStrings.length; i++) {
                returnValue[i] = Integer.parseInt(portStrings[i]);
            }

            return returnValue;
        }, false);

        if (ports != null) {
            List<PortBinding> portBindings = new ArrayList<>();
            for (int port : ports) {
                portBindings.add(new PortBinding(Ports.Binding.bindPort(port), new ExposedPort(port)));
            }

            container.withCreateContainerCmdModifier((Consumer<CreateContainerCmd>) cmd ->
                    cmd.withHostConfig(new HostConfig().withPortBindings(portBindings))
            );
        }

        container.withReuse(true);

        if (container instanceof JdbcDatabaseContainer) {
            ((JdbcDatabaseContainer) container).withUsername(getProperty(env, "username", String.class));
            ((JdbcDatabaseContainer) container).withPassword(getProperty(env, "password", String.class));
        }
    }

    @Override
    public void start() {
        container.start();
    }

    @Override
    public void stop() {
        container.stop();
    }

    @Override
    public Connection openConnection() throws SQLException {
        return DriverManager.getConnection(((JdbcDatabaseContainer) container).getJdbcUrl(), ((JdbcDatabaseContainer) container).getUsername(), ((JdbcDatabaseContainer) container).getPassword());
    }
}
