package liquibase.changelog.contentextractor;

import liquibase.changelog.RawChangeSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static liquibase.changelog.contentextractor.common.CommonExtractUtilities.*;

public class JsonChangeSetContentExtractor {
    public List<RawChangeSet> extractJsonChangeSets(String content, String changeLogFormat) {
        List<RawChangeSet> changeSets = new ArrayList<>();

        // JSON pattern to match changeSet blocks
        Pattern changeSetPattern = Pattern.compile(
                "\"changeSet\"\\s*:\\s*\\{([^}]+(?:\\{[^}]*\\}[^}]*)*)\\}",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );

        Matcher matcher = changeSetPattern.matcher(content);

        while (matcher.find()) {
            String changeSetBlock = matcher.group(1);
            Map<String, String> attributeMap = parseJsonAttributes(changeSetBlock);

            String author = attributeMap.get("author");
            String id = attributeMap.get("id");

            RawChangeSet rawChangeSet = new RawChangeSet(author, id, "");
            rawChangeSet.setChangeLogFormat(changeLogFormat);
            setAttributesFromMap(rawChangeSet, attributeMap);
            extractJsonPreconditions(rawChangeSet, content, matcher.start(), matcher.end());

            changeSets.add(rawChangeSet);
        }

        return changeSets;
    }

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

    private void extractJsonPreconditions(RawChangeSet changeSet, String content, int changeSetStart, int changeSetEnd) {
        String changeSetBlock = content.substring(changeSetStart, changeSetEnd);

        List<String> preconditionNames = new ArrayList<>();

        // Find the preConditions array in JSON format
        Pattern preConditionsPattern = Pattern.compile(
                "\"preConditions\"\\s*:\\s*\\[(.*?)\\]",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );

        Matcher preConditionsMatcher = preConditionsPattern.matcher(changeSetBlock);

        if (preConditionsMatcher.find()) {
            String preConditionsContent = preConditionsMatcher.group(1);

            // Pattern to find individual precondition objects
            Pattern preconditionObjectPattern = Pattern.compile(
                    "\\{\\s*\"(\\w+)\"\\s*:",
                    Pattern.CASE_INSENSITIVE
            );

            Matcher preconditionMatcher = preconditionObjectPattern.matcher(preConditionsContent);
            while (preconditionMatcher.find()) {
                String preconditionName = preconditionMatcher.group(1);
                if (!preconditionName.isEmpty()) {
                    preconditionNames.add(preconditionName);
                }
            }
        }
        changeSet.setPreconditions(preconditionNames);
    }
}
