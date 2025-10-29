package liquibase.parser;

import liquibase.ContextExpression;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.resource.ResourceAccessor;

/**
 * Defines the methods a class which is able to parse a Liquibase changelog file must support. Different parsers
 * are able to parse different file formats (e.g. XML, YAML, JSON, ...)
 */
public interface ChangeLogParser extends LiquibaseParser {

    String DATABASE_CHANGE_LOG = "databaseChangeLog";
  
    /**
     * Parses a Liquibase database changelog and returns the parsed form as an object.
     *
     * @param physicalChangeLogLocation the physical location of the changelog. The exact file formats and locations
     *                                  where can load changelog files from depend on the implementations and capabilities of the implementing parsers.
     * @param changeLogParameters       parameters given by the end user that should be applied while parsing the changelog
     *                                  (i.e. replacement of ${placeholders} inside the changelogs with user-defined content)
     * @param resourceAccessor          a Java resource accessor
     * @return the parsed ChangeLog in object form
     * @throws ChangeLogParseException if an error occurs during parsing of the ChangeLog
     */
    DatabaseChangeLog parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters,
                            ResourceAccessor resourceAccessor) throws ChangeLogParseException;


    /**
     * Parses a Liquibase database changelog and returns the parsed form as an object.
     *
     * @param physicalChangeLogLocation the physical location of the changelog. The exact file formats and locations
     *                                  where can load changelog files from depend on the implementations and capabilities of the implementing parsers.
     * @param changeLogParameters       parameters given by the end user that should be applied while parsing the changelog
     *                                  (i.e. replacement of ${placeholders} inside the changelogs with user-defined content)
     * @param resourceAccessor          a Java resource accessor
     * @param includeContextFilter      a context filter passed down from the parent. Set on include
     * @return the parsed ChangeLog in object form
     * @throws ChangeLogParseException if an error occurs during parsing of the ChangeLog
     */
    DatabaseChangeLog parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters,
                            ResourceAccessor resourceAccessor, ContextExpression includeContextFilter) throws ChangeLogParseException;

    /**
     * Checks if the file format is supported by an implementing ChangeLogParser and returns true if that is the case.
     *
     * @param changeLogFile    the location of the changelog file
     * @param resourceAccessor the resource accessor
     * @return true if the file format is supported, false if it is not.
     */
    boolean supports(String changeLogFile, ResourceAccessor resourceAccessor);
}
