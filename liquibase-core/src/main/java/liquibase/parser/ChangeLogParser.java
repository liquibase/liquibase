package liquibase.parser;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ChangeLogParameter;
import liquibase.exception.ChangeLogParseException;
import liquibase.resource.ResourceAccessor;

import java.util.Map;
import java.util.List;

public interface ChangeLogParser {

    public DatabaseChangeLog parse(String physicalChangeLogLocation, List<ChangeLogParameter> changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException;

    public String[] getValidFileExtensions();

}
