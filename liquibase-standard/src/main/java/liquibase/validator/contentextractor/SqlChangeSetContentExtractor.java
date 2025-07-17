package liquibase.validator.contentextractor;

import liquibase.validator.RawChangeSet;
import liquibase.changelog.filter.ChangeSetFilterResult;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static liquibase.validator.contentextractor.common.CommonExtractUtilities.*;

/**
 * This class will be used to extract changeset content from SQL formatted changelog and set the extracted content to a ChangeSet object (refer to {@link RawChangeSet}) which will be used for validation.
 */
public class SqlChangeSetContentExtractor {

    @Getter
    private final List<ChangeSetFilterResult> validationErrors = new ArrayList<>();
    private Map<String, String> changeSetAttributes = new HashMap<>();

    public SqlChangeSetContentExtractor() {
    }

    public List<String> getPropertiesFound() {
        if(!changeSetAttributes.isEmpty()) {
            return new ArrayList<>(changeSetAttributes.keySet());
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Extracts changesets with attributes/values we want to validate from the provided SQL content.
     *
     * @param content         The SQL content as a string.
     * @param changeLogFormat The format of the changelog ("sql" in this case).
     * @return A list of {@link RawChangeSet} objects extracted from the SQL content.
     */
    public List<RawChangeSet> extractSqlChangeSets(String content, String changeLogFormat) {
        List<RawChangeSet> changeSets = new ArrayList<>();

        // Find all changeset lines first
        String[] lines = content.split("\\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // Check if this line is a changeset declaration
            Pattern changeSetPattern = Pattern.compile(
                    "--changeset\\s+([^:]+):([^\\s]+)(?:\\s+(.*))?",
                    Pattern.CASE_INSENSITIVE
            );

            Matcher matcher = changeSetPattern.matcher(line);

            if (matcher.matches()) {
                String author = matcher.group(1).trim();
                String id = matcher.group(2).trim();
                String attributes = matcher.group(3) != null ? matcher.group(3).trim() : "";

                RawChangeSet rawChangeSet = new RawChangeSet(author, id, "");
                rawChangeSet.setChangeLogFormat(changeLogFormat);

                extractSqlAttributes(rawChangeSet, attributes);

                // Pass the content and the line index for precondition extraction
                extractSqlPreconditions(rawChangeSet, lines, i + 1);
                changeSets.add(rawChangeSet);
            }
        }

        return changeSets;
    }

    /**
     * Sets attributes from a map to the given {@link RawChangeSet} with the attribute values we want to validate.
     *
     * @param changeSet The changeSet to set attributes on.
     * @param attributes The map containing attribute names and values.
     */
    private void extractSqlAttributes(RawChangeSet changeSet, String attributes) {
        if (attributes == null || attributes.trim().isEmpty()) {
            return;
        }

        this.changeSetAttributes = parseChangeSetAttributes(attributes);
        setAttributesFromMap(changeSet, this.changeSetAttributes);
    }

    /**
     * Extract the precondition from the SQL content and sets them on the given {@link RawChangeSet}.
     * NOTE: This method assumes that SQL changeSet will contain only a precondition.
     *
     * @param changeSet The changeSet to set preconditions on.
     * @param lines     The lines of the SQL content.
     * @param startLineIndex The index to start looking for preconditions.
     */
    private void extractSqlPreconditions(RawChangeSet changeSet, String[] lines, int startLineIndex) {
        List<String> preconditions = new ArrayList<>();

        // Look for preconditions starting from the line after the changeset declaration
        for (int i = startLineIndex; i < lines.length; i++) {
            String line = lines[i].trim();

            // Stop if we hit another changeset
            if (line.toLowerCase().startsWith("--changeset")) {
                break;
            }

            // Check if this line is a precondition
            Pattern preconditionPattern = Pattern.compile(
                    "--precondition-([\\w-]+)(?:\\s+(.*))?",
                    Pattern.CASE_INSENSITIVE
            );

            Matcher matcher = preconditionPattern.matcher(line);
            if (matcher.matches()) {
                String preconditionName = matcher.group(1);
                if (preconditionName != null) {
                    preconditions.add(preconditionName);
                }
            }
        }

        changeSet.setPreconditions(preconditions);
    }

    /**
     * Parses the attributes from a SQL changeSet string and returns them as a map.
     *
     * @param attributes The string containing the attributes of the changeSet.
     * @return A map containing attribute names and their corresponding values.
     */
    private Map<String, String> parseChangeSetAttributes(String attributes) {
        Map<String, String> attributeMap = new HashMap<>();

        if (attributes == null || attributes.trim().isEmpty()) {
            return attributeMap;
        }

        String normalizedAttributes = attributes.replaceAll("\\s+", " ").trim();

        // Pattern to find all attribute names and their positions
        Pattern attributeNamePattern = Pattern.compile("\\b(\\w+)\\s*:");
        Matcher nameMatcher = attributeNamePattern.matcher(normalizedAttributes);

        List<AttributePosition> attributePositions = new ArrayList<>();
        while (nameMatcher.find()) {
            attributePositions.add(new AttributePosition(
                    nameMatcher.group(1).toLowerCase(),
                    nameMatcher.start(),
                    nameMatcher.end()
            ));
        }

        for (int i = 0; i < attributePositions.size(); i++) {
            AttributePosition currentAttr = attributePositions.get(i);

            // Find where attribute's value starts after the colon separator
            int valueStart = currentAttr.colonEnd;

            // Skip any whitespace after the colon
            while (valueStart < normalizedAttributes.length() &&
                    Character.isWhitespace(normalizedAttributes.charAt(valueStart))) {
                valueStart++;
            }

            // Find where this attribute's value ends (before next attribute or end of string)
            int valueEnd;
            if (i + 1 < attributePositions.size()) {
                // There's a next attribute, so value ends before it
                AttributePosition nextAttr = attributePositions.get(i + 1);
                valueEnd = nextAttr.nameStart;

                while (valueEnd > valueStart &&
                        Character.isWhitespace(normalizedAttributes.charAt(valueEnd - 1))) {
                    valueEnd--;
                }
            } else {
                // This is the last attribute, value goes to end of string
                valueEnd = normalizedAttributes.length();
                while (valueEnd > valueStart &&
                        Character.isWhitespace(normalizedAttributes.charAt(valueEnd - 1))) {
                    valueEnd--;
                }
            }

            String attributeValue;
            if (valueStart >= valueEnd) {
                // No value found or empty value
                attributeValue = "";
            } else {
                attributeValue = normalizedAttributes.substring(valueStart, valueEnd);
                attributeValue = handleQuotedValue(attributeValue);

                // Clean up comma-separated values for specific attributes
                if (isCommaSeparatedAttribute(currentAttr.name) && !attributeValue.trim().isEmpty()) {
                    attributeValue = cleanCommaSeparatedValue(attributeValue);
                } else {
                    attributeValue = attributeValue.trim();
                }
            }
            attributeMap.put(currentAttr.name, attributeValue);
        }
        return attributeMap;
    }

    /**
     * Represents the position of an attribute in the SQL changeSet string.
     */
    private static class AttributePosition {
        final String name;
        final int nameStart;
        final int colonEnd;

        AttributePosition(String name, int nameStart, int colonEnd) {
            this.name = name;
            this.nameStart = nameStart;
            this.colonEnd = colonEnd;
        }
    }
}
