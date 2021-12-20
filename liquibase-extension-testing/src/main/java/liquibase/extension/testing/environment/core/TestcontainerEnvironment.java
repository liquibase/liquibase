package liquibase.extension.testing.environment.core;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.extension.testing.environment.TestEnvironment;
import liquibase.util.StringUtil;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.util.ISO8601Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class TestcontainerEnvironment extends TestEnvironment {

    protected final GenericContainer container;

    public TestcontainerEnvironment(String env) {
        super(env);

        Class containerClass = getProperty(env, "containerClass", Class.class, true);
        String imageName = getProperty(env, "imageName", String.class);
        String imageVersion = getProperty(env, "imageVersion", String.class);

        try {
            container = (GenericContainer) containerClass.newInstance();
        } catch (Throwable e) {
            throw new UnexpectedLiquibaseException("Cannot instantiate " + containerClass.getName() + " for " + env + ": " + e.getMessage(), e);
        }

        if (imageName != null || imageVersion != null) {
            final String[] docker = container.getDockerImageName().split(":");
            if (imageName != null) {
                docker[0] = imageName;
            }
            if (imageVersion != null) {
                docker[1] = imageVersion;
            }
            container.setDockerImageName(docker[0] + ":" + docker[1]);
        }

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
            final String username = getProperty(env, "username", String.class);
            if (username != null) {
                ((JdbcDatabaseContainer) container).withUsername(username);
            }


            final String password = getProperty(env, "password", String.class);
            if (password != null) {
                ((JdbcDatabaseContainer) container).withPassword(password);
            }

            final String catalog = getProperty(env, "catalog.default", String.class);
            if (catalog != null) {
                ((JdbcDatabaseContainer) container).withDatabaseName(catalog);
            }
        }
    }

    @Override
    public String getUsername() {
        return ((JdbcDatabaseContainer) container).getUsername();
    }

    @Override
    public String getPassword() {
        return ((JdbcDatabaseContainer) container).getPassword();
    }

    @Override
    public String getUrl() {
        return ((JdbcDatabaseContainer) container).getJdbcUrl();
    }

    @Override
    public void start() throws Exception {
        if (container.isRunning()) {
            return;
        }

        container.start();

        final Date started = ISO8601Utils.parse(container.getCurrentContainerInfo().getCreated(), new ParsePosition(0));
        if (new Date().getTime() - started.getTime() < 60 * 1000) { //it just started
            final String initScript = getProperty(getEnv(), "init.script", String.class);
            if (initScript != null) {
                final Connection connection;
                final String initUsername = getProperty(getEnv(), "init.username", String.class);

                if (initUsername == null) {
                    connection = openConnection();
                } else {
                    connection = openConnection(initUsername, getProperty(getEnv(), "init.password", String.class));
                }

                final Statement statement = connection.createStatement();
                for (String sql : StringUtil.splitSQL(initScript, ";")) {
                    statement.execute(sql);
                }
            }
        }
    }

    @Override
    public void stop() {
        container.stop();
    }

    public Connection openConnection(String username, String password) throws SQLException {
        return DriverManager.getConnection(((JdbcDatabaseContainer) container).getJdbcUrl(), username, password);
    }

    @Override
    public Connection openConnection() throws SQLException {
        return openConnection(((JdbcDatabaseContainer) container).getUsername(), ((JdbcDatabaseContainer) container).getPassword());
    }
}
