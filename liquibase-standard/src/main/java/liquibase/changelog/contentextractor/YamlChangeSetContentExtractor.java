package liquibase.changelog.contentextractor;

import liquibase.changelog.RawChangeSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static liquibase.changelog.contentextractor.common.CommonExtractUtilities.*;

public class YamlChangeSetContentExtractor {

    public List<RawChangeSet> extractYamlChangeSets(String content, String changeLogFormat) {
        List<RawChangeSet> changeSets = new ArrayList<>();

        // YAML pattern to match changeSet blocks
        Pattern changeSetPattern = Pattern.compile(
                "-\\s+changeSet:\\s*\\n((?:\\s{2,}[^\\n]*\\n?)*)",
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

    private Map<String, String> parseYamlAttributes(String yamlBlock) {
        Map<String, String> attributeMap = new HashMap<>();

        if (yamlBlock == null || yamlBlock.trim().isEmpty()) {
            return attributeMap;
        }

        // Split the YAML block into lines and process each line
        String[] lines = yamlBlock.split("\\n");

        for (String line : lines) {
            // Skip empty lines and lines that are too deeply nested (like changes:, createTable:, etc.)
            if (line.trim().isEmpty()) {
                continue;
            }

            // Count leading whitespace to determine if this is a top-level changeset property
            int leadingSpaces = 0;
            for (char c : line.toCharArray()) {
                if (c == ' ') {
                    leadingSpaces++;
                } else {
                    break;
                }
            }

            // Only process lines with 2-8 spaces (changeset properties)
            // Skip deeply nested properties like changes:, createTable:, etc.
            if (leadingSpaces < 2 || leadingSpaces > 8) {
                continue;
            }

            String trimmedLine = line.trim();
            if (!trimmedLine.contains(":")) {
                continue;
            }

            int colonIndex = trimmedLine.indexOf(':');
            String attributeName = trimmedLine.substring(0, colonIndex).trim().toLowerCase();
            String attributeValue = trimmedLine.substring(colonIndex + 1).trim();

            // Skip nested properties we don't care about
            if ("changes".equals(attributeName) || "preconditions".equals(attributeName)) {
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

    private void extractYamlPreconditions(RawChangeSet changeSet, String content, int changeSetStart, int changeSetEnd) {
        String yamlChangeSetContentBlock = content.substring(changeSetStart, changeSetEnd);

        List<String> preconditionNames = new ArrayList<>();
        int preConditionsStart = yamlChangeSetContentBlock.indexOf("preConditions:");
        if (preConditionsStart == -1) {
            changeSet.setPreconditions(preconditionNames);
            return;
        }

        // Find the end of preConditions block by looking for the next top-level property
        // (changes:, rollback:, or any other property at the same indentation level as preConditions)
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

            // If a line with same or less indentation than preConditions is found that will indicate the end has been reached
            if (!currentLine.trim().isEmpty() && getIndentationLevel(currentLine) <= preConditionsIndentation) {
                break;
            }

            preConditionsContent.append(currentLine).append("\n");
        }

        String[] preConditionLines = preConditionsContent.toString().split("\\n");

        for (String line : preConditionLines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) {
                continue;
            }
            // Look for lines that start with "- " (direct precondition entries)
            if (trimmedLine.startsWith("- ")) {
                String preconditionLine = trimmedLine.substring(2).trim(); // Remove "- "
                String preconditionName;
                if (preconditionLine.contains(":")) {
                    preconditionName = preconditionLine.substring(0, preconditionLine.indexOf(":")).trim();
                } else {
                    preconditionName = preconditionLine.trim();
                }

                if (!preconditionName.isEmpty()) {
                    preconditionNames.add(preconditionName);
                }
            }
        }
        changeSet.setPreconditions(preconditionNames);
    }

    /**
     * Helper method to count leading spaces in a line
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
