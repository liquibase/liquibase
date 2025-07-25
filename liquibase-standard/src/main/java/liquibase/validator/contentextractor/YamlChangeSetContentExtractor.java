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
 * This class will be used to extract changeset content from YAML formatted changelog and set the extracted content to a ChangeSet object (refer to {@link RawChangeSet}) which will be used for validation.
 */
public class YamlChangeSetContentExtractor {

    /**
     * Extracts changesets with attributes/values we want to validate from the provided YAML content.
     *
     * @param content         The YAML content as a string.
     * @param changeLogFormat The format of the changelog ("yaml" in this case).
     * @return A list of {@link RawChangeSet} objects extracted from the YAML/YML content.
     */
    public List<RawChangeSet> extractYamlChangeSets(String content, String changeLogFormat) {
        List<RawChangeSet> changeSets = new ArrayList<>();

        // YAML pattern to match changeSet blocks
        Pattern changeSetPattern = Pattern.compile(
                "-\\s+changeSet:\\s*\\n((?:\\s{2,}+[^\\n]*+\\n?+)*+)",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
        );
        Matcher matcher = changeSetPattern.matcher(content);

        while (matcher.find()) {
            String changeSetBlock = matcher.group(1);
            Map<String, String> attributeMap = parseYamlAttributes(changeSetBlock);

            String author = attributeMap.get("author");
            String id = attributeMap.get("id");

            RawChangeSet rawChangeSet = new RawChangeSet(author, id, "");
            rawChangeSet.setChangeLogFormat(changeLogFormat);

            setAttributesFromMap(rawChangeSet, attributeMap);
            extractYamlPreconditions(rawChangeSet, content, matcher.start(), matcher.end());
            changeSets.add(rawChangeSet);
        }
        return changeSets;
    }

    /**
     * Parses the attributes with and their values from an YAML changeSet string and returns them as a map.
     *
     * @param yamlBlock The string containing the attributes of the changeSet.
     * @return A map containing attribute names and their corresponding values.
     */
    private Map<String, String> parseYamlAttributes(String yamlBlock) {
        Map<String, String> attributeMap = new HashMap<>();

        if (yamlBlock == null || yamlBlock.trim().isEmpty()) {
            return attributeMap;
        }

        // Split the YAML block into lines and process each line
        String[] lines = yamlBlock.split("\\n");

        for (String line : lines) {
            // Count leading whitespace to determine if this is a top-level changeset property
            int leadingSpaces = 0;
            for (int i = 0; i < line.length() && line.charAt(i) == ' '; i++) {
                leadingSpaces++;
            }

            String trimmedLine = line.trim();
            int colonIndex = trimmedLine.indexOf(':');
            String attributeName = trimmedLine.substring(0, colonIndex).trim().toLowerCase();
            String attributeValue = trimmedLine.substring(colonIndex + 1).trim();

            // Only process lines with 2-8 spaces (changeset properties)
            // Skip deeply nested properties like changes:, createTable:, etc.
            if (trimmedLine.isEmpty() || leadingSpaces < 2 || leadingSpaces > 8 || !trimmedLine.contains(":") || "changes".equals(attributeName) || "preconditions".equals(attributeName)) {
                continue;
            }

            if (attributeValue.isEmpty()) {
                attributeValue = "";
            } else {
                if ((attributeValue.startsWith("\"") && attributeValue.endsWith("\"")) ||
                        (attributeValue.startsWith("'") && attributeValue.endsWith("'"))) {
                    attributeValue = attributeValue.substring(1, attributeValue.length() - 1);
                }
                if (isCommaSeparatedAttribute(attributeName) && !attributeValue.trim().isEmpty()) {
                    attributeValue = cleanCommaSeparatedValue(attributeValue);
                } else {
                    attributeValue = attributeValue.trim();
                }
            }
            attributeMap.put(attributeName, attributeValue);
        }

        return attributeMap;
    }

    /**
     * Extracts preconditions and its nested preconditions from the YAML content block and sets them in the provided RawChangeSet.
     *
     * @param changeSet        The RawChangeSet to set preconditions on.
     * @param content          The full YAML content as a string.
     * @param changeSetStart   The start index of the changeSet in the content.
     * @param changeSetEnd     The end index of the changeSet in the content.
     */
    private void extractYamlPreconditions(RawChangeSet changeSet, String content, int changeSetStart, int changeSetEnd) {
        String yamlChangeSetContentBlock = content.substring(changeSetStart, changeSetEnd);

        List<String> preconditionNames = new ArrayList<>();
        int preConditionsStart = yamlChangeSetContentBlock.indexOf("preConditions:");
        if (preConditionsStart == -1) {
            changeSet.setPreconditions(preconditionNames);
            return;
        }

        // Find the end of preConditions block by looking for the next top-level property
        String[] allLines = yamlChangeSetContentBlock.split("\\n");
        int preConditionsLineIndex = -1;

        // Find which line contains "preconditions:"
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].trim().startsWith("preConditions:")) {
                preConditionsLineIndex = i;
                break;
            }
        }

        if (preConditionsLineIndex == -1) {
            changeSet.setPreconditions(preconditionNames);
            return;
        }

        int preConditionsIndentation = getIndentationLevel(allLines[preConditionsLineIndex]);
        StringBuilder preConditionsContent = new StringBuilder();
        for (int i = preConditionsLineIndex + 1; i < allLines.length; i++) {
            String currentLine = allLines[i];

            // If a line with same or less indentation than preConditions is found, the end has been reached
            if (!currentLine.trim().isEmpty() && getIndentationLevel(currentLine) <= preConditionsIndentation) {
                break;
            }

            preConditionsContent.append(currentLine).append("\n");
        }

        // Extract all precondition names recursively from the preConditions block
        extractAllYamlPreconditionNames(preConditionsContent.toString(), preconditionNames);

        changeSet.setPreconditions(preconditionNames);
    }

    /**
     * Recursively extracts all precondition names from the YAML content.
     *
     * @param yamlContent       The YAML content as a string.
     * @param preconditionNames The list to store extracted precondition names.
     */
    private void extractAllYamlPreconditionNames(String yamlContent, List<String> preconditionNames) {
        String[] lines = yamlContent.split("\\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmedLine = line.trim();

            if (trimmedLine.isEmpty()) {
                continue;
            }

            // Check if this line defines a precondition (has a property name followed by colon)
            if (trimmedLine.contains(":")) {
                String preconditionName = extractPreconditionName(trimmedLine);

                if (preconditionName != null && !preconditionName.isEmpty()) {
                    preconditionNames.add(preconditionName);

                    // Extract nested content and process recursively
                    String nestedContent = extractYamlNestedContent(lines, i);
                    if (nestedContent != null && !nestedContent.trim().isEmpty()) {
                        extractAllYamlPreconditionNames(nestedContent, preconditionNames);
                    }
                }
            }
        }
    }

    private String extractPreconditionName(String trimmedLine) {
        String preconditionName = null;

        // Handle array item with precondition: "- preconditionName:"
        if (trimmedLine.startsWith("- ")) {
            String preconditionLine = trimmedLine.substring(2).trim(); // Remove "- "
            if (preconditionLine.contains(":")) {
                preconditionName = preconditionLine.substring(0, preconditionLine.indexOf(":")).trim();
            }
        }
        // Handle direct property: "preconditionName:" or "preconditionName: value"
        else if (!trimmedLine.startsWith("-")) {
            preconditionName = trimmedLine.substring(0, trimmedLine.indexOf(":")).trim();
        }

        return preconditionName;
    }

    private String extractYamlNestedContent(String[] lines, int startLineIndex) {
        if (startLineIndex >= lines.length) {
            return null;
        }

        String startLine = lines[startLineIndex];
        int baseIndentation = getIndentationLevel(startLine);

        StringBuilder nestedContent = new StringBuilder();

        // Look for content that is more indented than the current line
        for (int i = startLineIndex + 1; i < lines.length; i++) {
            String currentLine = lines[i];

            int currentIndentation = getIndentationLevel(currentLine);
            // If line is empty, include it (to preserve structure)
            if (currentLine.trim().isEmpty() || currentIndentation > baseIndentation) {
                nestedContent.append(currentLine).append("\n");
            } else {  // If we hit a line with same or less indentation, we've reached the end of nested content
                break;
            }

            // This line is part of the nested content
            nestedContent.append(currentLine).append("\n");
        }

        return nestedContent.toString();
    }

    /**
     * Counts the number of leading spaces in a line to determine its indentation level.
     *
     * @param line The line to check.
     * @return The number of leading spaces in the line.
     */
    private int getIndentationLevel(String line) {
        int count = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

}
