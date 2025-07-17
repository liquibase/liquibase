package liquibase.validator.contentextractor.common;

import liquibase.validator.RawChangeSet;

import java.util.Map;

public class CommonExtractUtilities {

    private CommonExtractUtilities() {}

    public static String handleQuotedValue(String value) {
        String trimmedValue = value.trim();

        if (trimmedValue.startsWith("\"") && trimmedValue.endsWith("\"") && trimmedValue.length() >= 2) {
            return trimmedValue.substring(1, trimmedValue.length() - 1);
        }

        // Handle single quoted values
        if (trimmedValue.startsWith("'") && trimmedValue.endsWith("'") && trimmedValue.length() >= 2) {
            return trimmedValue.substring(1, trimmedValue.length() - 1);
        }

        return trimmedValue;
    }

    public static boolean isCommaSeparatedAttribute(String attributeName) {
        return "dbms".equals(attributeName) ||
                "labels".equals(attributeName) ||
                "context".equals(attributeName);
    }

    public static String cleanCommaSeparatedValue(String value) {
        if (value == null || !value.contains(",")) {
            return value;
        }

        String[] parts = value.split(",");
        StringBuilder cleaned = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (!part.isEmpty()) {
                if (cleaned.length() > 0) {
                    cleaned.append(",");
                }
                cleaned.append(part);
            }
        }

        return cleaned.toString();
    }

    public static void setAttributesFromMap(RawChangeSet changeSet, Map<String, String> attributesMap) {
        changeSet.setDbms(attributesMap.get("dbms"));
        changeSet.setRunWith(attributesMap.get("runwith"));
        changeSet.setContexts(attributesMap.get("context"));
        changeSet.setLabels(attributesMap.get("labels"));
        changeSet.setLogicalFilePath(attributesMap.get("logicalfilepath"));

        String quotingStrategy = attributesMap.get("objectquotingstrategy");
        if (quotingStrategy != null) {
            changeSet.setQuotingStrategy(RawChangeSet.QuotingStrategyEnum.fromString(quotingStrategy));
        }

        String onValidationFail = attributesMap.get("onvalidationfail");
        if (onValidationFail != null) {
            changeSet.setOnValidationFail(RawChangeSet.OnValidationFailEnum.fromString(onValidationFail));
        }
    }

}
