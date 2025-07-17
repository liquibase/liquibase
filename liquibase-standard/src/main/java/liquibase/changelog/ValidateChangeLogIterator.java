package liquibase.changelog;

import liquibase.Scope;
import liquibase.validator.contentextractor.JsonChangeSetContentExtractor;
import liquibase.validator.contentextractor.SqlChangeSetContentExtractor;
import liquibase.validator.contentextractor.XmlChangeSetContentExtractor;
import liquibase.validator.contentextractor.YamlChangeSetContentExtractor;
import liquibase.changelog.filter.*;
import liquibase.validator.RawChangeSet;
import liquibase.validator.propertyvalidator.ValidatorFilter;
import liquibase.exception.LiquibaseException;
import lombok.Getter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/** * This class iterates will read a DatabaseChangeLog content, and depending on its format(SQL, XML, YAML, or JSON) will ask a content extractor to extract the set of changeSets to validate
 * and then will apply a series of validation filters to each change set. It will collect any validation errors encountered during the process.
 */
public class ValidateChangeLogIterator {

    @Getter
    private final List<ChangeSetFilterResult> validationErrors = new ArrayList<>();
    private final ValidatorFilter[] validators;
    DatabaseChangeLog changeLog;

    public ValidateChangeLogIterator(DatabaseChangeLog changeLog, ValidatorFilter... validatorFilters) {
        this.changeLog = changeLog;
        this.validators = validatorFilters;
    }

    public void run() throws LiquibaseException {
        try {
            Map<String, Object> scopeSetttings = new HashMap<>();
            scopeSetttings.put(Scope.Attr.databaseChangeLog.name(), this.changeLog);
            Scope.child(scopeSetttings, () -> {
                String changelogPath = this.changeLog.getFilePath();
                String fileContent = readFileContent(changelogPath);
                String fileFormat = determineFileFormat(changelogPath);

                List<RawChangeSet> rawChangeSets = extractRawChangeSets(fileContent, fileFormat);

                for (RawChangeSet rawChangeSet : rawChangeSets) {
                    for(ValidatorFilter filter : validators) {
                        ChangeSetFilterResult changeSetResult = filter.accepts(rawChangeSet);
                        if(!changeSetResult.isAccepted()) {
                            validationErrors.add(changeSetResult);
                        }
                    }
                }
            });
        } catch(Exception e) {
            throw new LiquibaseException(e);
        }
    }

    private String readFileContent(String changelogPath) throws IOException {
        Path path = Paths.get(changelogPath);
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private String determineFileFormat(String changelogPath) {
        if (changelogPath.toLowerCase().endsWith(".xml")) {
            return "xml";
        } else if (changelogPath.toLowerCase().endsWith(".json")) {
            return "json";
        } else if (changelogPath.toLowerCase().endsWith(".yaml") || changelogPath.toLowerCase().endsWith(".yml")) {
            return "yaml";
        } else if (changelogPath.toLowerCase().endsWith(".sql")) {
            return "sql";
        } else {
            return "unknown";
        }
    }

    private List<RawChangeSet> extractRawChangeSets(String content, String format) {
        switch (format) {
            case "sql":  return new SqlChangeSetContentExtractor().extractSqlChangeSets(content, "sql");
            case "xml":  return new XmlChangeSetContentExtractor().extractXmlChangeSets(content, "xml");
            case "yaml": return new YamlChangeSetContentExtractor().extractYamlChangeSets(content, "yaml");
            case "json": return new JsonChangeSetContentExtractor().extractJsonChangeSets(content, "json");
            default: return Collections.emptyList();
        }
    }

}
