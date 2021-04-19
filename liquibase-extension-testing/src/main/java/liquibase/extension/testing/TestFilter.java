package liquibase.extension.testing;

import liquibase.util.StringUtil;
import org.spockframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TestFilter {

    public static final String DB = "db";

    private final Map<String, String> include = new HashMap<>();
    private final Map<String, String> exclude = new HashMap<>();

    public static final TestFilter instance;

    static {
        final String includeKey = "liquibase.integrationtest.include";
        final String excludeKey = "liquibase.integrationtest.exclude";

        String includeString = "";
        String excludeString = "";


        final String fileName = "liquibase/liquibase.integrationtest.local.properties";
        try (InputStream propertiesStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName)) {
            if (propertiesStream != null) {
                final Properties properties = new Properties();
                properties.load(propertiesStream);

                includeString = properties.getProperty(includeKey);
                excludeString = properties.getProperty(excludeKey);
            }
        } catch (IOException e) {
            System.out.println("Cannot load " + fileName + ": " + e.getMessage());
            e.printStackTrace();
        }

        includeString = System.getProperty(includeKey, includeString);
        excludeString = System.getProperty(excludeKey, excludeString);

        if (StringUtil.isNotEmpty(includeString) && StringUtil.isNotEmpty(excludeString)) {
            System.out.println("Integration test filtering: ");
            System.out.println("    " + includeKey + ": " + includeString);
            System.out.println("    " + excludeKey + ": " + excludeString);
        }

        instance = new TestFilter(includeString, excludeString);

    }

    public static TestFilter getInstance() {
        return instance;
    }

    private TestFilter(String include, String exclude) {
        parseAndAdd(include, "include", this.include);
        parseAndAdd(exclude, "exclude", this.exclude);
    }

    private void parseAndAdd(String input, String desc, Map<String, String> output) {
        if (input == null || input.equals("")) {
            return;
        }
        for (String inputPart : input.split("\\s*,\\s*")) {
            String[] keyValue = inputPart.split("\\s*:\\s*");
            if (keyValue.length != 2) {
                Assert.fail("Cannot parse " + desc + " " + inputPart);
            }
            output.put(keyValue[0], keyValue[1]);
        }
    }

    public boolean shouldRun(String key, String value) {
        if (value == null) {
            return true;
        }

        final String includeValue = include.get(key);
        if (value.equals(includeValue)) {
            return true;
        }

        final String excludeValue = exclude.get(key);
        if (value.equals(excludeValue)) {
            return false;
        }

        if (includeValue != null) {
            return false;
        }

        return true;
    }
}
