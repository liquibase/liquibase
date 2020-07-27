package liquibase.hub.core;

import liquibase.Scope;
import liquibase.configuration.HubConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.hub.LiquibaseHubException;
import liquibase.hub.LiquibaseHubObjectNotFoundException;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class HttpClient {

    private Yaml yaml;

    protected HttpClient() {
        DumperOptions dumperOptions = new DumperOptions();

        dumperOptions.setPrettyFlow(true);
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.FLOW);
        dumperOptions.setDefaultScalarStyle(DumperOptions.ScalarStyle.DOUBLE_QUOTED);
        dumperOptions.setWidth(Integer.MAX_VALUE);

        yaml = new Yaml(new Constructor(), new HubRepresenter(), dumperOptions);

        yaml.setBeanAccess(BeanAccess.FIELD);


    }

    protected <T> T doGet(String url, Class<T> returnType) throws LiquibaseHubException {
        return doGet(url, new HashMap<>(), returnType);

    }

    protected <T> T doGet(String url, Map<String, String> parameters, Class<T> returnType) throws LiquibaseHubException {
        if (parameters != null && parameters.size() > 0) {
            url += "?" + toQueryString(parameters);
        }

        return doRequest("GET", url, null, returnType);
    }

    protected <T> T doPost(String url, Object requestBodyObject, Class<T> returnType) throws LiquibaseHubException {
        return doRequest("POST", url, requestBodyObject, returnType);
    }

    protected <T> T doPut(String url, Object requestBodyObject, Class<T> returnType) throws LiquibaseHubException {
        return doRequest("PUT", url, requestBodyObject, returnType);
    }

    protected <T> T doDelete(String url, Class<T> returnType) throws LiquibaseHubException {
        return doRequest("DELETE", url, null, returnType);
    }

    private URLConnection openConnection(String url) throws LiquibaseHubException {
        HubConfiguration hubConfiguration = LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class);
        String hubUrl = hubConfiguration.getLiquibaseHubUrl();
        String apiKey = hubConfiguration.getLiquibaseHubApiKey();

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

    private <T> T doRequest(String method, String url, Object requestBodyObject, Class<T> returnType) throws LiquibaseHubException {
        try {
            HttpURLConnection connection = (HttpURLConnection) openConnection(url);
            if (requestBodyObject != null) {
                connection.setDoOutput(true);
            }
            connection.setRequestMethod(method);

            if (requestBodyObject != null) {
                String requestBody = yaml.dumpAs(requestBodyObject, Tag.MAP, DumperOptions.FlowStyle.FLOW);
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(requestBody.getBytes(StandardCharsets.UTF_8));
                }
            }

            try (InputStream response = connection.getInputStream()) {
                return (T) yaml.loadAs(response, returnType);
            } catch (IOException e) {
                try {
                    try (InputStream error = connection.getErrorStream()) {
                        if (error != null) {
                            final Map errorDetails = yaml.load(error);

                            LiquibaseHubException returnException = new LiquibaseHubException((String) errorDetails.get("message"), e);

                            if (connection.getResponseCode() == 404) {
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
        } catch (IOException e) {
            throw new LiquibaseHubException(e);
        }
    }


    private static class HubRepresenter extends Representer {

        HubRepresenter() {
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
