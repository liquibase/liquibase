package liquibase.hub.core;

import liquibase.Scope;
import liquibase.changelog.RanChangeSet;
import liquibase.configuration.HubConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.LiquibaseException;
import liquibase.hub.HubService;
import liquibase.hub.LiquibaseHubException;
import liquibase.hub.LiquibaseHubObjectNotFoundException;
import liquibase.hub.model.*;
import liquibase.plugin.Plugin;
import liquibase.util.ISODateFormat;
import liquibase.util.LiquibaseUtil;
import liquibase.util.StringUtil;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;

public class OnlineHubService implements HubService {

    private Yaml yaml;
    private UUID organizationId;

    public OnlineHubService() {
        DumperOptions dumperOptions = new DumperOptions();

        dumperOptions.setPrettyFlow(true);
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.FLOW);
        dumperOptions.setDefaultScalarStyle(DumperOptions.ScalarStyle.DOUBLE_QUOTED);
        dumperOptions.setWidth(Integer.MAX_VALUE);

        yaml = new Yaml(new Constructor(), new HubRepresenter(), dumperOptions);

        yaml.setBeanAccess(BeanAccess.FIELD);

    }

    @Override
    public int getPriority() {
        return Plugin.PRIORITY_DEFAULT;
    }

    @Override
    public HubUser getMe() throws LiquibaseHubException {
        final Map response = doGet("/api/v1/users/me", Map.class);

        HubUser user = new HubUser();
        user.setId(UUID.fromString((String) response.get("id")));
        user.setUsername((String) response.get("userName"));

        return user;
    }

    @Override
    public Organization getOrganization() throws LiquibaseHubException {
        final Map<String, List<Map>> response = doGet("/api/v1/organizations", Map.class);

        Organization org = new Organization();
        List<Map> contentList = response.get("content");
        if (organizationId == null) {
            String id = (String) contentList.get(0).get("id");
            if (id != null) {
                organizationId = UUID.fromString(id);
            }
        }
        org.setId(organizationId);
        String name = (String) contentList.get(0).get("name");
        org.setName(name);

        return org;
    }

    @Override
    public List<Project> getProjects() throws LiquibaseHubException {
        final UUID organizationId = getOrganization().getId();

        final Map<String, List<Map>> response = doGet("/api/v1/organizations/" + organizationId.toString() + "/projects", Map.class);
        List<Map> contentList = response.get("content");
        List<Project> returnList = new ArrayList<>();
        for (int i = 0; i < contentList.size(); i++) {
            String id = (String) contentList.get(0).get("id");
            String name = (String) contentList.get(i).get("name");

            Project project = new Project();
            project.setId(UUID.fromString(id));
            project.setName(name);
            returnList.add(project);
        }

        return returnList;
    }

    @Override
    public HubChangeLog createChangeLog(Project project) throws LiquibaseException {
        final UUID organizationId = getOrganization().getId();

        HubChangeLog changeLogRequest = new HubChangeLog();
        changeLogRequest.setExternalChangeLogId(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"));
        changeLogRequest.setFileName("string");
        changeLogRequest.setName("string");
        return doPost("/api/v1/organizations/" + organizationId.toString() + "/projects/" + project.getId() + "/changelogs", changeLogRequest, HubChangeLog.class);
    }

    @Override
    public void setRanChangeSets(UUID environmentId, List<RanChangeSet> ranChangeSets) throws LiquibaseHubException {

    }

    @Override
    public List<Environment> getEnvironments(Environment exampleEnvironment) throws LiquibaseHubException {
        final Organization organization = getOrganization();

        final Map response = doGet("/api/v1/organizations/" + organization.getId() + "/environments", Collections.singletonMap("search", toSearchString(exampleEnvironment)), Map.class);

        List<Environment> returnList = new ArrayList<>();

        try {
            for (Map object : (List<Map>) response.get("content")) {
                returnList.add(new Environment()
                        .setId(UUID.fromString((String) object.get("id")))
                        .setJdbcUrl((String) object.get("jdbcUrl"))
                        .setName((String) object.get("name"))
                        .setDescription((String) object.get("description"))
                        .setCreateDate(parseDate((String) object.get("createDate")))
                        .setUpdateDate(parseDate((String) object.get("updateDate")))
                        .setRemoveDate(parseDate((String) object.get("removeDate")))
                );
            }
        } catch (ParseException e) {
            throw new LiquibaseHubException(e);
        }

        return returnList;
    }

    protected Date parseDate(String stringDate) throws ParseException {
        return new ISODateFormat().parse(stringDate);
    }

    @Override
    public Environment createEnvironment(UUID projectId, Environment environment) throws LiquibaseHubException {
        return doPost("/api/v1/organizations/" + getOrganization().getId() + "/projects/" + projectId + "/environments", environment, Environment.class);
    }

    @Override
    public HubChangeLog getChangeLog(String changeLogId) {
        return null;
    }

    protected <T> T doGet(String url, Class<T> returnType) throws LiquibaseHubException {
        return doGet(url, new HashMap<>(), returnType);

    }

    protected <T> T doGet(String url, Map<String, String> parameters, Class<T> returnType) throws LiquibaseHubException {
        if (parameters != null && parameters.size() > 0) {
            url += "?" + toQueryString(parameters);
        }

        URLConnection connection = openConnection(url);

        return doRequest(connection, returnType);
    }

    protected <T> T doPost(String url, Object requestBodyObject, Class<T> returnType) throws LiquibaseHubException {
        HttpURLConnection connection = (HttpURLConnection) openConnection(url);
        connection.setDoOutput(true);

        String requestBody = createPostRequestBody(requestBodyObject);
        try {
            try (OutputStream output = connection.getOutputStream()) {
                output.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }
            return doRequest(connection, returnType);
        } catch (IOException e) {
            throw new LiquibaseHubException(e);
        }
    }

    private String createPostRequestBody(Object requestObjectBody) {
        return yaml.dumpAs(requestObjectBody, Tag.MAP, DumperOptions.FlowStyle.FLOW);
    }

    private URLConnection openConnection(String url) throws LiquibaseHubException {
        HubConfiguration hubConfiguration = LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class);
        String hubUrl = hubConfiguration.getLiquibaseHubUrl();
        String apiKey = hubConfiguration.getLiquibaseHubApiKey(); //"961e43af-b1db-43ac-a001-e0b1719891a4";

        try {
            final URLConnection connection = new URL(hubUrl + url).openConnection();
            connection.setRequestProperty("User-Agent", "Liquibase " + LiquibaseUtil.getBuildVersion());
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
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

        return StringUtil.join(paramArray, "&");
    }

    private <T> T doRequest(URLConnection connection, Class<T> returnType) throws LiquibaseHubException {
        try (InputStream response = connection.getInputStream()) {
            return (T) yaml.loadAs(response, returnType);
        } catch (IOException e) {
            try {
                try (InputStream error = ((HttpURLConnection) connection).getErrorStream()) {
                    if (error != null) {
                        final Map errorDetails = yaml.load(error);

                        LiquibaseHubException returnException = new LiquibaseHubException((String) errorDetails.get("message"), e);

                        if (((HttpURLConnection) connection).getResponseCode() == 404) {
                            returnException = new LiquibaseHubObjectNotFoundException(returnException.getMessage(), returnException.getCause());
                        }
                        returnException.setTimestamp((String) errorDetails.get("timestamp"));
                        returnException.setDetails((String) errorDetails.get("details"));
                        throw returnException;

                    }
                }
            } catch (IOException ioException) {
                Scope.getCurrentScope().getLog(getClass()).info("Cannot read request error stream", e);
            }

            throw new LiquibaseHubException(e);
        }
    }

    /**
     * Converts an object to a search string. Any properties with non-null values are used as search arguments
     */
    protected String toSearchString(Object object) {
        if (object == null) {
            return "";
        }

        SortedSet<String> clauses = new TreeSet<>();
        final Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(object);
                if (value != null) {
                    value = value.toString().replace("\"", "\\\"");
                    clauses.add(field.getName() + ":\"" + value + "\"");
                }

            } catch (IllegalAccessException ignored) {
                //don't use it as a param
            }
        }

        return StringUtil.join(clauses, " AND ");

    }

    private static class HubRepresenter extends Representer {

        public HubRepresenter() {
            getPropertyUtils().setSkipMissingProperties(true);
        }

        @Override
        protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue, Tag customTag) {
            if (propertyValue == null) {
                return null;
            }
            return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
        }
    }
}
