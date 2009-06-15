package liquibase.parser;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.resource.ResourceAccessor;

import java.util.Map;

public interface ChangeLogParser {

    public DatabaseChangeLog parse(String physicalChangeLogLocation, Map<String, Object> changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException;

    public String[] getValidFileExtensions();

}
