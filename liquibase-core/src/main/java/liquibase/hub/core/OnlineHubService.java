package liquibase.hub.core;

import com.sun.deploy.util.StringUtils;
import liquibase.Scope;
import liquibase.hub.HubService;
import liquibase.hub.LiquibaseHubException;
import liquibase.hub.model.HubUser;
import liquibase.hub.model.Organization;
import liquibase.hub.model.Project;
import liquibase.plugin.Plugin;
import liquibase.util.LiquibaseUtil;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtil;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class OnlineHubService implements HubService {

    @Override
    public int getPriority() {
        return Plugin.PRIORITY_DEFAULT;
    }

    @Override
    public HubUser getMe() throws LiquibaseHubException {
        final Map response = doGet("/api/v1/users/me", Map.class);

        HubUser user = new HubUser();
        user.setId(UUID.fromString((String) response.get("id")));
        user.setUsername((String) response.get("username"));

        return user;
    }

    @Override
    public Organization getOrganization() throws LiquibaseHubException {
        final Map response = doGet("/api/v1/organizations", Map.class);

        Organization org = new Organization();
        org.setId(UUID.fromString((String) response.get("id")));
        org.setName((String) response.get("name"));

        return org;
    }

    @Override
    public List<Project> getProjects() throws LiquibaseHubException {
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setName("Sample project");

        final UUID organizationId = getOrganization().getId();

        final Map response = doGet("/api/v1/organization/" + organizationId.toString() + "/projects", Map.class);
        System.out.println("Got response " + response.size());

        return Collections.singletonList(project);
    }

    protected <T> T doGet(String url, Class<T> returnType) throws LiquibaseHubException {
        return doGet(url, new HashMap<>(), returnType);

    }

    protected <T> T  doGet(String url, Map<String, String> parameters, Class<T> returnType) throws LiquibaseHubException {
        if (parameters != null && parameters.size() > 0) {
            url += "?" + toQueryString(parameters);
        }

        URLConnection connection = openConnection(url);

        return doRequest(connection, returnType);
    }

    protected <T> T doPost(String url, Map<String, String> parameters, Class<T> returnType) throws LiquibaseHubException {
        if (parameters != null && parameters.size() > 0) {
            url += toQueryString(parameters);
        }

        URLConnection connection = openConnection(url);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

        try {
            try (OutputStream output = connection.getOutputStream()) {
                output.write(toQueryString(parameters).getBytes(StandardCharsets.UTF_8));
            }

            return doRequest(connection, returnType);
        } catch (IOException e) {
            throw new LiquibaseHubException(e);
        }
    }

    private URLConnection openConnection(String url) throws LiquibaseHubException {
        String apiKey = "961e43af-b1db-43ac-a001-e0b1719891a4";

        try {
            final URLConnection connection = new URL("http://localhost:8888" + url).openConnection();
            connection.setRequestProperty("User-Agent", "Liquibase " + LiquibaseUtil.getBuildVersion());
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            return connection;
        } catch (IOException e) {
            throw new LiquibaseHubException(e);
        }
    }

    private String toQueryString(Map<String, String> parameters) {
        if (parameters == null) {
            return "";
        }

        List<String> paramArray = new ArrayList<>();
        try {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                paramArray.add(entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return StringUtils.join(paramArray, "&");
    }

    private <T> T doRequest(URLConnection connection, Class<T> returnType) throws LiquibaseHubException {
        try (InputStream response = connection.getInputStream()) {
            Yaml yaml = new Yaml(new SafeConstructor());
            return (T) yaml.load(response);
        } catch (IOException e) {
            try {
                try (InputStream error = ((HttpURLConnection) connection).getErrorStream()) {
                    final String errorStream = StringUtil.trimToNull(StreamUtil.readStreamAsString(error));

                    if (errorStream != null) {
                        throw new LiquibaseHubException(errorStream, e);
                    }
                }
            } catch (IOException ioException) {
                Scope.getCurrentScope().getLog(getClass()).info("Cannot read request error stream", e);
            }

            throw new LiquibaseHubException(e);
        }
    }
}
