package liquibase.validator.contentextractor;

import liquibase.validator.RawChangeSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static liquibase.validator.contentextractor.common.CommonExtractUtilities.*;

/**
 * This class will be used to extract changeset content from JSON formatted changelog and set the extracted content to a ChangeSet object (refer to {@link RawChangeSet}) which will be used for validation.
 */
public class JsonChangeSetContentExtractor {
    /**
     * Extracts changesets with attributes/values we want to validate from the provided JSON content.
     *
     * @param content         The JSON content as a string.
     * @param changeLogFormat The format of the changelog ("json" in this case).
     * @return A list of {@link RawChangeSet} objects extracted from the JSON content.
     */
    public List<RawChangeSet> extractJsonChangeSets(String content, String changeLogFormat) {
        List<RawChangeSet> changeSets = new ArrayList<>();

        // Better approach: Find changeSet objects by counting braces properly
        Pattern changeSetPattern = Pattern.compile(
                "\"changeSet\"\\s*:\\s*\\{",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = changeSetPattern.matcher(content);

        while (matcher.find()) {
            // Find the matching closing brace for this changeSet
            int start = matcher.end() - 1; // Position of opening brace
            int braceCount = 1;
            int end = start + 1;

            // Count braces to find the matching closing brace
            while (end < content.length() && braceCount > 0) {
                char c = content.charAt(end);
                if (c == '{') {
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                }
                end++;
            }

            if (braceCount == 0) {
                // Found complete changeSet block
                String changeSetBlock = content.substring(start + 1, end - 1); // Exclude the outer braces

                Map<String, String> attributeMap = parseJsonAttributes(changeSetBlock);

                String author = attributeMap.get("author");
                String id = attributeMap.get("id");

                if (author != null && id != null) {
                    RawChangeSet rawChangeSet = new RawChangeSet(author, id, "");
                    rawChangeSet.setChangeLogFormat(changeLogFormat);
                    setAttributesFromMap(rawChangeSet, attributeMap);

                    // Pass the complete changeSet block for precondition extraction
                    extractJsonPreconditions(rawChangeSet, changeSetBlock);

                    changeSets.add(rawChangeSet);
                }
            }
        }

        return changeSets;
    }

    /**
     * Parses the attributes with their values from a JSON changeSet block and returns them as a map.
     *
     * @param jsonBlock The string containing the JSON attributes of the changeSet.
     * @return A map containing attribute names and their corresponding values.
     */
    private Map<String, String> parseJsonAttributes(String jsonBlock) {
        Map<String, String> attributeMap = new HashMap<>();

        if (jsonBlock == null || jsonBlock.trim().isEmpty()) {
            return attributeMap;
        }

        // Pattern to match JSON "key": "value" pairs
        Pattern attributePattern = Pattern.compile(
                "\"(\\w+)\"\\s*:\\s*\"([^\"]*?)\"",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = attributePattern.matcher(jsonBlock);

        while (matcher.find()) {
            String attributeName = matcher.group(1).toLowerCase();
            String attributeValue = matcher.group(2);

            if ("changes".equals(attributeName) || "preconditions".equals(attributeName)) {
                continue;
            }

            if (attributeValue != null) {
                if (isCommaSeparatedAttribute(attributeName) && !attributeValue.trim().isEmpty()) {
                    attributeValue = cleanCommaSeparatedValue(attributeValue);
                } else {
                    attributeValue = attributeValue.trim();
                }
                attributeMap.put(attributeName, attributeValue);
            } else {
                attributeMap.put(attributeName, "");
            }
        }
        return attributeMap;
    }

    /**
     * Extracts preconditions from the JSON changeSet block and sets them in the provided RawChangeSet object.
     *
     * @param changeSet      The RawChangeSet object to set preconditions on.
     * @param changeSetBlock The JSON block of the changeSet containing preconditions.
     */
    private void extractJsonPreconditions(RawChangeSet changeSet, String changeSetBlock) {
        List<String> preconditionNames = new ArrayList<>();

        // Find the preConditions array in JSON format
        Pattern preConditionsPattern = Pattern.compile(
                "\"preConditions\"\\s*:\\s*\\[(.*?)\\]",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );

        Matcher preConditionsMatcher = preConditionsPattern.matcher(changeSetBlock);

        if (preConditionsMatcher.find()) {
            String preConditionsContent = preConditionsMatcher.group(1);

            // Extract all precondition names recursively from the entire preconditions block
            extractAllPreconditionNames(preConditionsContent, preconditionNames);
        }
        changeSet.setPreconditions(preconditionNames);
    }

    private void extractAllPreconditionNames(String jsonContent, List<String> preconditionNames) {
        // Pattern to find all property names followed by objects (not primitive values)
        Pattern preconditionPattern = Pattern.compile(
                "\"(\\w+)\"\\s*:\\s*\\{",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = preconditionPattern.matcher(jsonContent);

        while (matcher.find()) {
            String propertyName = matcher.group(1);

            // Only consider properties that have object values as potential preconditions
            // This automatically excludes primitive properties like "tableName": "users"
            preconditionNames.add(propertyName);

            // Recursively search within the nested object
            String nestedContent = extractNestedContent(jsonContent, matcher.start());
            if (nestedContent != null && !nestedContent.trim().isEmpty()) {
                extractAllPreconditionNames(nestedContent, preconditionNames);
            }
        }

        // Also handle array-type preconditions like "or": [...], "and": [...]
        Pattern arrayPreconditionPattern = Pattern.compile(
                "\"(\\w+)\"\\s*:\\s*\\[",
                Pattern.CASE_INSENSITIVE
        );

        Matcher arrayMatcher = arrayPreconditionPattern.matcher(jsonContent);
        while (arrayMatcher.find()) {
            String propertyName = arrayMatcher.group(1);
            preconditionNames.add(propertyName);

            // Recursively search within the array
            String nestedContent = extractNestedArrayContent(jsonContent, arrayMatcher.start());
            if (nestedContent != null && !nestedContent.trim().isEmpty()) {
                extractAllPreconditionNames(nestedContent, preconditionNames);
            }
        }
    }

    private String extractNestedContent(String content, int startPos) {
        // Find the opening brace after the property name
        int openPos = -1;

        for (int i = startPos; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') {
                openPos = i;
                break;
            }
        }

        if (openPos == -1) {
            return null;
        }

        // Find the matching closing brace
        int braceCount = 1;
        int closePos = openPos + 1;

        while (closePos < content.length() && braceCount > 0) {
            char c = content.charAt(closePos);
            if (c == '{') {
                braceCount++;
            } else if (c == '}') {
                braceCount--;
            }
            closePos++;
        }

        if (braceCount == 0) {
            // Return the content between the opening and closing braces
            return content.substring(openPos + 1, closePos - 1);
        }

        return null;
    }

    private String extractNestedArrayContent(String content, int startPos) {
        // Find the opening bracket after the property name
        int openPos = -1;

        for (int i = startPos; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '[') {
                openPos = i;
                break;
            }
        }

        if (openPos == -1) {
            return null;
        }

        // Find the matching closing bracket
        int bracketCount = 1;
        int closePos = openPos + 1;

        while (closePos < content.length() && bracketCount > 0) {
            char c = content.charAt(closePos);
            if (c == '[') {
                bracketCount++;
            } else if (c == ']') {
                bracketCount--;
            }
            closePos++;
        }

        if (bracketCount == 0) {
            // Return the content between the opening and closing brackets
            return content.substring(openPos + 1, closePos - 1);
        }

        return null;
    }
}
