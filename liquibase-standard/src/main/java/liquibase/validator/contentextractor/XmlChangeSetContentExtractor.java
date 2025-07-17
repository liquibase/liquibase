package liquibase.validator.contentextractor;

import liquibase.validator.RawChangeSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static liquibase.validator.contentextractor.common.CommonExtractUtilities.*;

public class XmlChangeSetContentExtractor {

    public List<RawChangeSet> extractXmlChangeSets(String content, String changeLogFormat) {
        List<RawChangeSet> changeSets = new ArrayList<>();

        // XML pattern to match changeSet blocks
        Pattern changeSetPattern = Pattern.compile(
                "<changeSet\\s+([^>]+)>",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );

        Matcher matcher = changeSetPattern.matcher(content);

        while (matcher.find()) {
            String attributes = matcher.group(1);
            Map<String, String> attributeMap = parseXmlAttributes(attributes);

            String author = attributeMap.get("author");
            String id = attributeMap.get("id");
            RawChangeSet rawChangeSet = new RawChangeSet(author, id, "");
            rawChangeSet.setChangeLogFormat(changeLogFormat);

            setAttributesFromMap(rawChangeSet, attributeMap);
            changeSets.add(rawChangeSet);
        }
        return changeSets;
    }

    private Map<String, String> parseXmlAttributes(String attributesString) {
        Map<String, String> attributeMap = new HashMap<>();

        if (attributesString == null || attributesString.trim().isEmpty()) {
            return attributeMap;
        }

        // Pattern to match XML attribute="value" or attribute='value'
        Pattern attributePattern = Pattern.compile(
                "(\\w+)\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)')",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = attributePattern.matcher(attributesString);
        while (matcher.find()) {
            String attributeName = matcher.group(1).toLowerCase();
            String attributeValue = matcher.group(2) != null ? matcher.group(2) : matcher.group(3);

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
}
