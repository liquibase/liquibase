package liquibase.changelog.contentextractor;

import liquibase.changelog.RawChangeSet;
import liquibase.changelog.filter.ChangeSetFilterResult;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static liquibase.changelog.contentextractor.common.CommonExtractUtilities.*;

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

    public List<RawChangeSet> extractSqlChangeSets(String content, String changeLogFormat) {
        List<RawChangeSet> changeSets = new ArrayList<>();

        // SQL pattern to match changeSet blocks
        Pattern changeSetPattern = Pattern.compile(
                "--changeset\\s+([^:]+):([^\\s]+)(?:\\s+(.*))?$",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
        );

        Matcher matcher = changeSetPattern.matcher(content);

        while (matcher.find()) {
            String author = matcher.group(1).trim();
            String id = matcher.group(2).trim();
            String attributes = matcher.group(3) != null ? matcher.group(3).trim() : "";

            RawChangeSet rawChangeSet = new RawChangeSet(author, id, "");
            rawChangeSet.setChangeLogFormat(changeLogFormat);

            extractSqlAttributes(rawChangeSet, attributes);
            extractSqlPreconditions(rawChangeSet, content, matcher.end());
            changeSets.add(rawChangeSet);
        }

        return changeSets;
    }

    private void extractSqlAttributes(RawChangeSet changeSet, String attributes) {
        if (attributes == null || attributes.trim().isEmpty()) {
            return;
        }

        this.changeSetAttributes = parseChangeSetAttributes(attributes);
        setAttributesFromMap(changeSet, this.changeSetAttributes);
    }

    private void extractSqlPreconditions(RawChangeSet changeSet, String content, int startPos) {
        // Look for preconditions after the changeset declaration until the next changeset or end of file
        String afterChangeSet = content.substring(startPos);

        // Find the end of this changeset (next --changeset or end of file)
        Pattern nextChangesetPattern = Pattern.compile("--changeset", Pattern.CASE_INSENSITIVE);
        Matcher nextChangesetMatcher = nextChangesetPattern.matcher(afterChangeSet);

        String changesetContent;
        if (nextChangesetMatcher.find()) {
            changesetContent = afterChangeSet.substring(0, nextChangesetMatcher.start());
        } else {
            changesetContent = afterChangeSet;
        }

        // Pattern for SQL preconditions in the format: --precondition-[type] [parameters]
        Pattern preconditionPattern = Pattern.compile(
                "^\\s*--precondition-([\\w-]+)(?:\\s+(.*))?$",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
        );

        Matcher matcher = preconditionPattern.matcher(changesetContent);

        List<String> preconditions = null;
        if (matcher.find()) {
            preconditions = new ArrayList<>();
            String preconditionName = matcher.group(1);
            preconditions.add(preconditionName);
        }
        changeSet.setPreconditions(preconditions);
    }

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
