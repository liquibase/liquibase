package liquibase.hub.core;

import liquibase.hub.LiquibaseHubException;

import java.util.*;

public class MockHttpClient extends HttpClient {

    protected Map<String, ?> responses = new HashMap<>();
    protected Map<String, Object> requests = new HashMap<>();

    public MockHttpClient() {
    }

    public MockHttpClient(Map<String, ?> responses) {
        this.responses = responses;
    }

    @Override
    protected <T> T doRequest(String method, String url, Object requestBodyObject, Class<T> returnType, Class contentReturnType) throws LiquibaseHubException {
        final String requestKey = method.toUpperCase() + " " + url;
        this.requests.put(requestKey, requestBodyObject);

        if (!responses.containsKey(requestKey)) {
            throw new RuntimeException("Unknown mock request for " + requestKey);
        }

        final Object response = responses.get(requestKey);
        if (response instanceof LiquibaseHubException) {
            throw (LiquibaseHubException) response;
        }
        return (T) response;
    }

    /**
     * Creates an API response of an array of objects
     */
    public static Map<String, ?> createListResponse(Map<String, ?>... objects) {
        Map<String, Object> returnMap = new LinkedHashMap<>();
        List<Map<String, ?>> content = new ArrayList<>();
        for (Map<String, ?> object : objects) {
            content.add(object);
        }

        returnMap.put("content", content);

        return returnMap;
    }

    public Object getRequestBody(String requestKey) {
        return requests.get(requestKey);
    }
}
