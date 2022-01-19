package liquibase.extension.testing.testsystem.wrapper;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.*;
import liquibase.Scope;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.extension.testing.testsystem.TestSystem;
import org.testcontainers.containers.JdbcDatabaseContainer;

import java.util.*;
import java.util.function.Consumer;

/**
 * Implementation of {@link DatabaseWrapper} for databases that are managed via docker in {@link JdbcDatabaseContainer}s.
 */
public class DockerDatabaseWrapper extends DatabaseWrapper {

    public static final String TEST_SYSTEM_LABEL = "org.liquibase.testSystem";

    private final JdbcDatabaseContainer container;
    private final TestSystem testSystem;

    private static final Set<String> alreadyRunningContainerIds = new HashSet<>();

    public DockerDatabaseWrapper(JdbcDatabaseContainer container, TestSystem testSystem) {
        this.container = container;
        this.testSystem = testSystem;
    }

    @Override
    public void start() throws Exception {
        if (container.isRunning()) {
            return;
        }

        final DockerClient dockerClient = container.getDockerClient();
        for (Container container : dockerClient.listContainersCmd().exec()) {
            final String containerTestSystem = container.getLabels().get(TEST_SYSTEM_LABEL);
            if (containerTestSystem != null && containerTestSystem.equals(testSystem.getDefinition().toString())) {
                break;
            }
        }

        container.withReuse(testSystem.getKeepRunning());

        if (testSystem.getKeepRunning()) {
            mapPorts(container);
        }

        container.withLabel(TEST_SYSTEM_LABEL, testSystem.getDefinition().toString());
        container.start();

        Scope.getCurrentScope().getLog(getClass()).info("Running " + testSystem.getDefinition() + " as container " + container.getContainerName());
    }

    protected void mapPorts(JdbcDatabaseContainer container) {
        int[] ports = testSystem.getConfiguredValue("ports", value -> {
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

        if (ports == null) {
            final List<Integer> exposedPorts = container.getExposedPorts();
            ports = new int[exposedPorts.size()];
            for (int i = 0; i < exposedPorts.size(); i++) {
                ports[i] = exposedPorts.get(i);

            }
        }

        if (ports != null) {
            List<PortBinding> portBindings = new ArrayList<>();
            for (int port : ports) {
                portBindings.add(new PortBinding(Ports.Binding.bindPort(port), new ExposedPort(port)));
            }

            container.withCreateContainerCmdModifier((Consumer<CreateContainerCmd>) cmd ->
                    cmd.withHostConfig(new HostConfig().withPortBindings(portBindings))
            );
        }
    }

    @Override
    public void stop() throws Exception {
        if (container.isRunning()) {
            container.stop();
        } else {
            final DockerClient dockerClient = container.getDockerClient();
            final List<Container> containers = dockerClient.listContainersCmd().withLabelFilter(Collections.singletonMap(TEST_SYSTEM_LABEL, testSystem.getDefinition().toString())).exec();
            if (containers.size() == 0) {
                throw new UnexpectedLiquibaseException("Cannot find running container " + testSystem.getDefinition().getName());
            } else {
                for (Container container : containers) {
                    Scope.getCurrentScope().getLog(getClass()).info("Stopping container " + container.getId());
                    dockerClient.stopContainerCmd(container.getId()).exec();
                }
            }
        }
    }

    @Override
    public String getUrl() {
        return container.getJdbcUrl();
    }

    public JdbcDatabaseContainer getContainer() {
        return container;
    }

    @Override
    public String getUsername() {
        return container.getUsername();
    }
}
