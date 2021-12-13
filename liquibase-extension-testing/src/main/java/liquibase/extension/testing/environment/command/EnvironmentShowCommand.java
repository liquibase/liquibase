package liquibase.extension.testing.environment.command;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import liquibase.Scope;
import liquibase.command.AbstractCommandStep;
import liquibase.command.CommandResultsBuilder;
import liquibase.ui.UIService;
import org.testcontainers.DockerClientFactory;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

public class EnvironmentShowCommand extends AbstractCommandStep {

    @Override
    public String[][] defineCommandNames() {
        return new String[][]{
                {"sdk", "env", "show"}
        };
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        final DockerClient dockerClient = DockerClientFactory.lazyClient();
        final List<Container> exec = dockerClient.listContainersCmd().exec();

        try (final PrintWriter out = new PrintWriter(resultsBuilder.getOutputStream())) {
            out.println("All Docker Containers: ");
            for (Container container : exec) {
                out.println(container.getImage() + " " + container.getStatus());
            }
        }
    }
}
