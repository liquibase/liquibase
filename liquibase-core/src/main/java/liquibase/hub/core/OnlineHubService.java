package liquibase.hub.core;

import liquibase.Scope;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.configuration.HubConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.LiquibaseException;
import liquibase.hub.*;
import liquibase.hub.model.*;
import liquibase.logging.Logger;
import liquibase.plugin.Plugin;
import liquibase.ui.ConsoleUIService;
import liquibase.util.ISODateFormat;
import liquibase.util.StringUtil;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.*;

public class OnlineHubService implements HubService {

    private Boolean available;
    private UUID organizationId;
    private UUID userId;

    private HttpClient http;

    public OnlineHubService() {
        this.http = createHttpClient();
    }

    public HttpClient createHttpClient() {
        return new HttpClient();
    }

    @Override
    public int getPriority() {
        if (isHubAvailable()) {
            return Plugin.PRIORITY_DEFAULT + 100;
        } else {
            return PRIORITY_NOT_APPLICABLE;
        }
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    public boolean isHubAvailable() {
        if (this.available == null) {
            final Logger log = Scope.getCurrentScope().getLog(getClass());
            final HubServiceFactory hubServiceFactory = Scope.getCurrentScope().getSingleton(HubServiceFactory.class);

            if (LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class).getLiquibaseHubMode().equalsIgnoreCase("OFF")) {
                hubServiceFactory.setOfflineReason("property liquibase.hub.mode is 'OFF'. To send data to Liquibase Hub, please set it to \"realtime\"");
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

                    log.info("Connected to Liquibase Hub with an API Key '" +  LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class).getLiquibaseHubApiKeySecureDescription() + "'");
                    this.available = true;
                } catch (LiquibaseHubException e) {
                    hubServiceFactory.setOfflineReason(e.getMessage());
                    log.info(e.getMessage(), e);
                    this.available = false;
                }
            }

            if (!this.available) {
                log.info("Not connecting to Liquibase Hub: "+ hubServiceFactory.getOfflineReason());
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
        final Map<String, List<Map>> response = http.doGet("/api/v1/organizations", Map.class);

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
    public Project createProject(Project project) throws LiquibaseException {
        final UUID organizationId = getOrganization().getId();

        return http.doPost("/api/v1/organizations/" + organizationId.toString() + "/projects", project, Project.class);
    }

    @Override
    public HubChangeLog createChangeLog(HubChangeLog hubChangeLog) throws LiquibaseException {
        final UUID organizationId = getOrganization().getId();

        return http.doPost("/api/v1/organizations/" + organizationId.toString() + "/projects/" + hubChangeLog.getPrj().getId() + "/changelogs", hubChangeLog, HubChangeLog.class);
    }

    @Override
    public void setRanChangeSets(Environment environment, List<RanChangeSet> ranChangeSets) throws LiquibaseHubException {
        List<HubChange> hubChangeList = new ArrayList<>();
        for (RanChangeSet ranChangeSet : ranChangeSets) {
            hubChangeList.add(new HubChange(ranChangeSet));
        }

        http.doPut("/api/v1/organizations/" + getOrganization().getId() + "/environments/" + environment.getId() + "/changes", hubChangeList, ArrayList.class);
    }

    @Override
    public Environment getEnvironment(Environment exampleEnvironment, boolean createIfNotExists) throws LiquibaseHubException {
        if (exampleEnvironment.getId() != null) {
            //do not auto-create if specifying the exact id
            return http.doGet("/api/v1/environments/" + exampleEnvironment.getId().toString(), null, Environment.class);
        }

        final List<Environment> environments;
        try {
            environments = getEnvironments(exampleEnvironment);
        } catch (LiquibaseHubObjectNotFoundException e) {
            //the API should not throw this exception, but it does
            if (createIfNotExists) {
                return createEnvironment(exampleEnvironment);
            } else {
                throw new LiquibaseHubObjectNotFoundException("Environment not found");
            }
        }
        if (environments.size() == 0) {
            if (createIfNotExists) {
                return createEnvironment(exampleEnvironment);
            } else {
                throw new LiquibaseHubObjectNotFoundException("Environment not found");
            }
        } else if (environments.size() == 1) {
            return environments.get(0);
        } else {
            throw new LiquibaseHubException("The url " + exampleEnvironment.getJdbcUrl() + " is used by more than one environment. Please specify 'hubEnvironmentId=<hubEnvironmentId>' or 'changeLogFile=<changeLogFileName>' in liquibase.properties or the command line");
        }
    }

    @Override
    public List<Environment> getEnvironments(Environment exampleEnvironment) throws LiquibaseHubException {
        final Organization organization = getOrganization();

        final ListResponse response;
        try {
            response = http.doGet("/api/v1/organizations/" + organization.getId() + "/environments", Collections.singletonMap("search", toSearchString(exampleEnvironment)), ListResponse.class, Environment.class);
        } catch (LiquibaseHubObjectNotFoundException e) {
            //Hub should not be returning this, but does
            return new ArrayList<>();
        }

        List<Environment> returnList = new ArrayList<>();

        try {
            for (Map object : (List<Map>) response.getContent()) {
                returnList.add(new Environment()
                        .setId(UUID.fromString((String) object.get("id")))
                        .setJdbcUrl((String) object.get("jdbcUrl"))
                        .setName((String) object.get("name"))
                        .setDescription((String) object.get("description"))
                        .setCreateDate(parseDate((String) object.get("createDate")))
                        .setUpdateDate(parseDate((String) object.get("updateDate")))
                        .setRemoveDate(parseDate((String) object.get("removeDate")))
                        .setPrj(exampleEnvironment.getPrj())
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
    public Environment createEnvironment(Environment environment) throws LiquibaseHubException {
        if (environment.getPrj() == null || environment.getPrj().getId() == null) {
            throw new LiquibaseHubUserException("projectId is required to create an environment");
        }

        //cannot send project information
        Environment sendEnvironment = new Environment()
                .setName(environment.getName())
                .setJdbcUrl(environment.getJdbcUrl())
                .setDescription(environment.getDescription());

        if (sendEnvironment.getName() == null) {
            sendEnvironment.setName(sendEnvironment.getJdbcUrl());
        }

        return http.doPost("/api/v1/organizations/" + getOrganization().getId() + "/projects/" + environment.getPrj().getId() + "/environments", sendEnvironment, Environment.class);
    }

    /**
     * Query for a changelog ID.  If no result we return null
     *
     * @param changeLogId Changelog ID for query
     * @return HubChangeLog               Object container for result
     * @throws LiquibaseHubException
     */
    @Override
    public HubChangeLog getChangeLog(UUID changeLogId) throws LiquibaseHubException {
        try {
            return http.doGet("/api/v1/changelogs/" + changeLogId, HubChangeLog.class);
        } catch (LiquibaseHubObjectNotFoundException lbe) {
            return null;
        }
    }

    @Override
    public Operation createOperation(String operationType, HubChangeLog changeLog, Environment environment, Map<String, String> operationParameters) throws LiquibaseHubException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("envId", environment.getId());
        requestBody.put("envJdbcUrl", environment.getJdbcUrl());
        requestBody.put("envName", environment.getName());
        requestBody.put("envDescription", environment.getDescription());
        requestBody.put("changelogId", changeLog.getId());
        requestBody.put("operationType", operationType);
        requestBody.put("operationStatusType", "PASS");
        requestBody.put("statusMessage", operationType);

        final Operation operation = http.doPost("/api/v1/operations", requestBody, Operation.class);
        operation.setEnvironment(environment);
        return operation;
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
            requestParams.put("logs", operationEvent.getOperationEventLog().getLogMessage());
            requestParams.put("logsTimestamp", operationEvent.getOperationEventLog().getTimestampLog());
        }

        return http.doPost("/api/v1/organizations/" + organization.getId() + "/projects/" + operation.getEnvironment().getPrj().getId() + "/operations/" + operation.getId() + "/operation-events", requestParams, OperationEvent.class);

    }

    @Override
    public void sendOperationChangeEvent(OperationChangeEvent operationChangeEvent) throws LiquibaseException {
        OperationChangeEvent sendOperationChangeEvent =
           new OperationChangeEvent()
              .setEventType(operationChangeEvent.getEventType())
              .setChangesetId(operationChangeEvent.getChangesetId())
              .setChangesetAuthor(operationChangeEvent.getChangesetAuthor())
              .setChangesetFilename(operationChangeEvent.getChangesetFilename())
              .setStartDate(operationChangeEvent.getStartDate())
              .setEndDate(operationChangeEvent.getEndDate())
              .setOperationStatusType(operationChangeEvent.getOperationStatusType())
              .setChangesetBody(operationChangeEvent.getChangesetBody())
              .setGeneratedSql(operationChangeEvent.getGeneratedSql())
              .setLogs(operationChangeEvent.getLogs())
              .setStatusMessage(operationChangeEvent.getStatusMessage())
              .setLogsTimestamp(operationChangeEvent.getLogsTimestamp());
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

}
