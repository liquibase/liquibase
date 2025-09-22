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
 * This class will be used to extract changeset content from XML formatted changelog and set the extracted content to a ChangeSet object (refer to {@link RawChangeSet}) which will be used for validation.
 */

public class XmlChangeSetContentExtractor {

    /**
     * Extracts changesets with attributes/values we want to validate from the provided XML content.
     * Note: for the XML format, we are most of the non-boolean attributes we will validate, except for Preconditions, which we will rely validation on XSD schema.
     *
     * @param content         The XML content as a string.
     * @param changeLogFormat The format of the changelog ("xml" in this case).
     * @return A list of {@link RawChangeSet} objects extracted from the XML content.
     */
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

    /**
     * Parses the attributes with and their values from an XML changeSet string and returns them as a map.
     *
     * @param attributesString The string containing the attributes of the changeSet.
     * @return A map containing attribute names and their corresponding values.
     */
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
