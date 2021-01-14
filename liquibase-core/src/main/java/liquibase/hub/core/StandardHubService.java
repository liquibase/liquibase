package liquibase.hub.core;

import liquibase.Scope;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.configuration.HubConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.LiquibaseException;
import liquibase.hub.*;
import liquibase.hub.model.*;
import liquibase.integration.IntegrationDetails;
import liquibase.logging.Logger;
import liquibase.plugin.Plugin;
import liquibase.util.ISODateFormat;
import liquibase.util.LiquibaseUtil;
import liquibase.util.StringUtil;

import java.lang.reflect.Field;
import java.net.ConnectException;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.*;

public class StandardHubService implements HubService {
    private static final String DATE_TIME_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private Boolean available;
    private UUID organizationId;
    private String organizationName;
    private UUID userId;
    private Map<UUID, HubChangeLog> hubChangeLogCache = new HashMap<>();

    private HttpClient http;

    public StandardHubService() {
        this.http = createHttpClient();
    }

    public HttpClient createHttpClient() {
        return new HttpClient();
    }

    @Override
    public int getPriority() {
        return Plugin.PRIORITY_DEFAULT + 100;
    }

    @Override
    public boolean isOnline() {
        return !LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class).getLiquibaseHubMode().equalsIgnoreCase("OFF");
    }

    public boolean isHubAvailable() {
        if (this.available == null) {
            final Logger log = Scope.getCurrentScope().getLog(getClass());
            final HubServiceFactory hubServiceFactory = Scope.getCurrentScope().getSingleton(HubServiceFactory.class);

            if (LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class).getLiquibaseHubMode().equalsIgnoreCase("OFF")) {
                hubServiceFactory.setOfflineReason("property liquibase.hub.mode is 'OFF'. To send data to Liquibase Hub, please set it to \"all\"");
                this.available = false;
            } else if (getApiKey() == null) {
                hubServiceFactory.setOfflineReason("liquibase.hub.apiKey was not specified");
                this.available = false;
            } else {
                try {
                    if (userId == null) {
                        HubUser me = this.getMe();
                        this.userId = me.getId();
                    }
                    if (organizationId == null) {
                        Organization organization = this.getOrganization();
                        this.organizationId = organization.getId();
                    }

                    log.info("Connected to Liquibase Hub with an API Key '" + LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class).getLiquibaseHubApiKeySecureDescription() + "'");
                    this.available = true;
                } catch (LiquibaseHubException e) {
                    if (e.getCause() instanceof ConnectException) {
                        hubServiceFactory.setOfflineReason("Cannot connect to Liquibase Hub");
                    } else {
                        hubServiceFactory.setOfflineReason(e.getMessage());
                    }
                    log.info(e.getMessage(), e);
                    this.available = false;
                }
            }
            String apiKey = getApiKey();
            if (!this.available && apiKey != null) {
                String message = "Hub communication failure: " + hubServiceFactory.getOfflineReason() + ".\n" +
                        "The data for your operations will not be recorded in your Liquibase Hub project";
                Scope.getCurrentScope().getUI().sendMessage(message);
                log.info(message);
            }
        }

        return this.available;
    }

    public String getApiKey() {
        HubConfiguration hubConfiguration = LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class);
        return StringUtil.trimToNull(hubConfiguration.getLiquibaseHubApiKey());
    }

    @Override
    public HubUser getMe() throws LiquibaseHubException {
        final Map response;
        try {
            response = http.doGet("/api/v1/users/me", Map.class);
        } catch (LiquibaseHubSecurityException e) {
            throw new LiquibaseHubSecurityException("Invalid Liquibase Hub api key", e);
        }

        HubUser user = new HubUser();
        user.setId(UUID.fromString((String) response.get("id")));
        user.setUsername((String) response.get("userName"));

        return user;
    }

    @Override
    public Organization getOrganization() throws LiquibaseHubException {
        if (organizationId == null) {
            final Map<String, List<Map>> response = http.doGet("/api/v1/organizations", Map.class);
            List<Map> contentList = response.get("content");
            String id = (String) contentList.get(0).get("id");
            if (id != null) {
                organizationId = UUID.fromString(id);
            }
            organizationName = (String) contentList.get(0).get("name");
        }
        Organization org = new Organization();
        org.setId(organizationId);
        org.setName(organizationName);
        return org;
    }

    @Override
    public Project getProject(UUID projectId) throws LiquibaseHubException {
        final UUID organizationId = getOrganization().getId();

        try {
            return http.doGet("/api/v1/organizations/" + organizationId.toString() + "/projects/" + projectId, Project.class);
        } catch (LiquibaseHubObjectNotFoundException lbe) {
            Scope.getCurrentScope().getLog(getClass()).severe(lbe.getMessage(), lbe);
            return null;
        }
    }

    @Override
    public List<Project> getProjects() throws LiquibaseHubException {
        final UUID organizationId = getOrganization().getId();

        final Map<String, List<Map>> response = http.doGet("/api/v1/organizations/" + organizationId.toString() + "/projects", Map.class);
        List<Map> contentList = response.get("content");
        List<Project> returnList = new ArrayList<>();
        for (int i = 0; i < contentList.size(); i++) {
            String id = (String) contentList.get(i).get("id");
            String name = (String) contentList.get(i).get("name");
            String dateString = (String) contentList.get(i).get("createDate");
            Date date = null;
            try {
                date = parseDate(dateString);
            } catch (ParseException dpe) {
                Scope.getCurrentScope().getLog(getClass()).warning("Project '" + name + "' has an invalid create date of '" + dateString + "'");
            }
            Project project = new Project();
            project.setId(UUID.fromString(id));
            project.setName(name);
            project.setCreateDate(date);
            returnList.add(project);
        }

        return returnList;
    }

    @Override
    public HubRegisterResponse register(String email) throws LiquibaseException {
        HubRegister hubRegister = new HubRegister();
        hubRegister.setEmail(email);
        try {
            HubRegisterResponse response = http.doPost("/api/v1/register", hubRegister, HubRegisterResponse.class);
            if (response.getApiKey() != null) {
                return response;
            }
        } catch (LiquibaseHubException e) {
            throw new LiquibaseException(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Project createProject(Project project) throws LiquibaseException {
        final UUID organizationId = getOrganization().getId();

        return http.doPost("/api/v1/organizations/" + organizationId.toString() + "/projects", project, Project.class);
    }

    @Override
    public HubChangeLog createChangeLog(HubChangeLog hubChangeLog) throws LiquibaseException {
        final UUID organizationId = getOrganization().getId();

        return http.doPost("/api/v1/organizations/" + organizationId.toString() + "/projects/" + hubChangeLog.getProject().getId() + "/changelogs", hubChangeLog, HubChangeLog.class);
    }

    @Override
    public void setRanChangeSets(Connection connection, List<RanChangeSet> ranChangeSets) throws LiquibaseHubException {
        List<HubChange> hubChangeList = new ArrayList<>();
        for (RanChangeSet ranChangeSet : ranChangeSets) {
            hubChangeList.add(new HubChange(ranChangeSet));
        }

        http.doPut("/api/v1/organizations/" + getOrganization().getId() + "/connections/" + connection.getId() + "/changes", hubChangeList, ArrayList.class);
    }

    @Override
    public Connection getConnection(Connection exampleConnection, boolean createIfNotExists) throws LiquibaseHubException {
        if (exampleConnection.getId() != null) {
            //do not auto-create if specifying the exact id
            return http.doGet("/api/v1/connections/" + exampleConnection.getId().toString(), null, Connection.class);
        }

        final List<Connection> connections;
        try {
            connections = getConnections(exampleConnection);
        } catch (LiquibaseHubObjectNotFoundException e) {
            //the API should not throw this exception, but it does
            if (createIfNotExists) {
                return createConnection(exampleConnection);
            } else {
                throw new LiquibaseHubObjectNotFoundException("Connection not found");
            }
        }
        if (connections.size() == 0) {
            if (createIfNotExists) {
                return createConnection(exampleConnection);
            } else {
                throw new LiquibaseHubObjectNotFoundException("Connection not found");
            }
        } else if (connections.size() == 1) {
            return connections.get(0);
        } else {
            throw new LiquibaseHubException("The url " + exampleConnection.getJdbcUrl() + " is used by more than one connection. Please specify 'hubConnectionId=<hubConnectionId>' or 'changeLogFile=<changeLogFileName>' in liquibase.properties or the command line");
        }
    }

    @Override
    public List<Connection> getConnections(Connection exampleConnection) throws LiquibaseHubException {
        final Organization organization = getOrganization();

        final ListResponse response;
        try {
            response = http.doGet("/api/v1/organizations/" + organization.getId() + "/connections", Collections.singletonMap("search", toSearchString(exampleConnection)), ListResponse.class, Connection.class);
        } catch (LiquibaseHubObjectNotFoundException e) {
            //Hub should not be returning this, but does
            return new ArrayList<>();
        }

        List<Connection> returnList = new ArrayList<>();

        try {
            for (Map object : (List<Map>) response.getContent()) {
                returnList.add(new Connection()
                        .setId(UUID.fromString((String) object.get("id")))
                        .setJdbcUrl((String) object.get("jdbcUrl"))
                        .setName((String) object.get("name"))
                        .setDescription((String) object.get("description"))
                        .setCreateDate(parseDate((String) object.get("createDate")))
                        .setUpdateDate(parseDate((String) object.get("updateDate")))
                        .setRemoveDate(parseDate((String) object.get("removeDate")))
                        .setProject(exampleConnection != null ? exampleConnection.getProject() : null)
                );
            }
        } catch (ParseException e) {
            throw new LiquibaseHubException(e);
        }

        return returnList;
    }

    protected Date parseDate(String stringDate) throws ParseException {
        if (stringDate == null) {
            return null;
        }
        return new ISODateFormat().parse(stringDate);
    }

    @Override
    public Connection createConnection(Connection connection) throws LiquibaseHubException {
        if (connection.getProject() == null || connection.getProject().getId() == null) {
            throw new LiquibaseHubUserException("projectId is required to create a connection");
        }

        //cannot send project information
        Connection sendConnection = new Connection()
                .setName(connection.getName())
                .setJdbcUrl(connection.getJdbcUrl())
                .setDescription(connection.getDescription());

        if (sendConnection.getName() == null) {
            sendConnection.setName(sendConnection.getJdbcUrl());
        }

        return http.doPost("/api/v1/organizations/" + getOrganization().getId() + "/projects/" + connection.getProject().getId() + "/connections", sendConnection, Connection.class);
    }


    @Override
    public String shortenLink(String url) throws LiquibaseException {
        HubLinkRequest reportHubLink = new HubLinkRequest();
        reportHubLink.url = url;

        return http.getHubUrl()+ http.doPut("/api/v1/links", reportHubLink, HubLink.class).getShortUrl();
    }

    /**
     * Query for a changelog ID.  If no result we return null
     * We cache this result and a map
     *
     * @param changeLogId Changelog ID for query
     * @return HubChangeLog               Object container for result
     * @throws LiquibaseHubException
     */
    @Override
    public HubChangeLog getHubChangeLog(UUID changeLogId) throws LiquibaseHubException {
        if (hubChangeLogCache.containsKey(changeLogId)) {
            return hubChangeLogCache.get(changeLogId);
        }
        try {
            HubChangeLog hubChangeLog = http.doGet("/api/v1/changelogs/" + changeLogId, HubChangeLog.class);
            hubChangeLogCache.put(changeLogId, hubChangeLog);
            return hubChangeLog;
        } catch (LiquibaseHubObjectNotFoundException lbe) {
            final String message = lbe.getMessage();
            String uiMessage = "Retrieving Hub Change Log failed for Changelog ID " + changeLogId.toString();
            Scope.getCurrentScope().getUI().sendMessage(uiMessage + ": " + message);
            Scope.getCurrentScope().getLog(getClass()).severe(message, lbe);
            return null;
        }
    }

    @Override
    public Operation createOperation(String operationType, HubChangeLog changeLog, Connection connection) throws LiquibaseHubException {

        String hostName;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (Throwable e) {
            Scope.getCurrentScope().getLog(getClass()).severe("Cannot determine hostname to send to hub", e);
            hostName = null;
        }

        final IntegrationDetails integrationDetails = Scope.getCurrentScope().get("integrationDetails", IntegrationDetails.class);

        Map<String, Object> clientMetadata = new HashMap<>();
        clientMetadata.put("liquibaseVersion", LiquibaseUtil.getBuildVersion());
        clientMetadata.put("hostName", hostName);
        clientMetadata.put("systemUser", System.getProperty("user.name"));
        clientMetadata.put("clientInterface", integrationDetails.getName());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("connectionId", connection.getId());
        requestBody.put("changelogId", changeLog.getId());
        requestBody.put("operationType", operationType);
        requestBody.put("operationStatusType", "PASS");
        requestBody.put("statusMessage", operationType);
        requestBody.put("clientMetadata", clientMetadata);
        requestBody.put("operationParameters", getCleanOperationParameters(integrationDetails.getParameters()));

        final Operation operation = http.doPost("/api/v1/operations", requestBody, Operation.class);
        operation.setConnection(connection);
        return operation;
    }

    protected Map<String, String> getCleanOperationParameters(Map<String, String> originalParams) {
        if (originalParams == null) {
            return null;
        }

        Set<String> paramsToRemove = new HashSet<>(Arrays.asList(
                "url",
                "username",
                "password",
                "apiKey",
                "classpath"
        ));

        final Map<String, String> returnMap = new HashMap<>();

        for (Map.Entry<String, String> param : originalParams.entrySet()) {
            boolean allowed = true;
            for (String skipKey : paramsToRemove) {
                if (param.getKey().toLowerCase().contains(skipKey.toLowerCase())) {
                    allowed = false;
                    break;
                }
            }

            if (allowed) {
                String value = param.getValue();
                if (param.getKey().toLowerCase().contains("liquibaseProLicenseKey".toLowerCase())) {
                    if (value != null && value.length() > 8) {
                        value = value.substring(0, 8) + "************";
                    }
                }

                returnMap.put(param.getKey(), value);
            }
        }

        return returnMap;
    }

    @Override
    public OperationEvent sendOperationEvent(Operation operation, OperationEvent operationEvent) throws LiquibaseException {
        final Organization organization = getOrganization();

        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("eventType", operationEvent.getEventType());
        requestParams.put("startDate", operationEvent.getStartDate());
        requestParams.put("endDate", operationEvent.getEndDate());

        if (operationEvent.getOperationEventStatus() != null) {
            requestParams.put("statusType", operationEvent.getOperationEventStatus().getOperationEventStatusType());
            requestParams.put("statusMessage", operationEvent.getOperationEventStatus().getStatusMessage());
            requestParams.put("operationEventStatusType", operationEvent.getOperationEventStatus().getOperationEventStatusType());
        }

        if (operationEvent.getOperationEventLog() != null) {
            if (!LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class).getLiquibaseHubMode().equalsIgnoreCase("meta")) {
                requestParams.put("logs", operationEvent.getOperationEventLog().getLogMessage());
                requestParams.put("logsTimestamp", operationEvent.getOperationEventLog().getTimestampLog());
            }
        }

        return http.doPost("/api/v1/organizations/" + organization.getId() + "/projects/" + operation.getConnection().getProject().getId() + "/operations/" + operation.getId() + "/operation-events", requestParams, OperationEvent.class);

    }

    private Date convertDateToUTC(Date dateInString) {
        //LocalDateTime ldt = LocalDateTime.parse(dateInString, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_STRING));
        return null;
    }

    @Override
    public void sendOperationChangeEvent(OperationChangeEvent operationChangeEvent) throws LiquibaseException {
        String changesetBody = null;
        String[] generatedSql = null;
        String logs = null;
        Date logsTimestamp = operationChangeEvent.getLogsTimestamp();
        if (!LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class).getLiquibaseHubMode().equalsIgnoreCase("meta")) {
            changesetBody = operationChangeEvent.getChangesetBody();
            generatedSql = operationChangeEvent.getGeneratedSql();

            logs = operationChangeEvent.getLogs();
        }


        OperationChangeEvent sendOperationChangeEvent =
                new OperationChangeEvent()
                        .setEventType(operationChangeEvent.getEventType())
                        .setChangesetId(operationChangeEvent.getChangesetId())
                        .setChangesetAuthor(operationChangeEvent.getChangesetAuthor())
                        .setChangesetFilename(operationChangeEvent.getChangesetFilename())
                        .setStartDate(operationChangeEvent.getStartDate())
                        .setEndDate(operationChangeEvent.getEndDate())
                        .setDateExecuted(operationChangeEvent.getDateExecuted())
                        .setOperationStatusType(operationChangeEvent.getOperationStatusType())
                        .setChangesetBody(changesetBody)
                        .setGeneratedSql(generatedSql)
                        .setLogs(logs)
                        .setStatusMessage(operationChangeEvent.getStatusMessage())
                        .setLogsTimestamp(logsTimestamp);
        http.doPost("/api/v1" +
                        "/organizations/" + getOrganization().getId().toString() +
                        "/projects/" + operationChangeEvent.getProject().getId().toString() +
                        "/operations/" + operationChangeEvent.getOperation().getId().toString() +
                        "/change-events",
                sendOperationChangeEvent, OperationChangeEvent.class);
    }

    @Override
    public void sendOperationChanges(OperationChange operationChange) throws LiquibaseHubException {
        List<HubChange> hubChangeList = new ArrayList<>();
        for (ChangeSet changeSet : operationChange.getChangeSets()) {
            hubChangeList.add(new HubChange(changeSet));
        }

        http.doPost("/api/v1" +
                        "/organizations/" + getOrganization().getId().toString() +
                        "/projects/" + operationChange.getProject().getId().toString() +
                        "/operations/" + operationChange.getOperation().getId().toString() +
                        "/changes",
                hubChangeList, ArrayList.class);
    }

    /**
     * Converts an object to a search string.
     * Any properties with non-null values are used as search arguments.
     * If a HubModel has an id specified, only that value is used in the search.
     */
    protected String toSearchString(HubModel object) {
        if (object == null) {
            return "";
        }
        SortedSet<String> clauses = new TreeSet<>();

        toSearchString(object, "", clauses);
        return StringUtil.join(clauses, " AND ");
    }

    private void toSearchString(HubModel object, String clausePrefix, SortedSet<String> clauses) {
        final Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                if (field.isSynthetic()) {
                    continue;
                }
                Object value = field.get(object);
                if (value != null) {
                    if (value instanceof HubModel) {
                        final UUID modelId = ((HubModel) value).getId();
                        if (modelId == null) {
                            String newPrefix = clausePrefix + field.getName() + ".";
                            newPrefix = newPrefix.replaceFirst("^\\.", "");
                            toSearchString((HubModel) value, newPrefix, clauses);
                        } else {
                            clauses.add(clausePrefix + field.getName() + ".id:\"" + modelId + "\"");
                        }
                    } else {
                        value = value.toString().replace("\"", "\\\"");
                        clauses.add(clausePrefix + field.getName() + ":\"" + value + "\"");
                    }
                }

            } catch (IllegalAccessException ignored) {
                //don't use it as a param
            }
        }


    }

    protected static class HubLinkRequest {
        protected String url;
    }

}
