package liquibase.changelog;

import liquibase.DatabaseChangeLog;
import liquibase.FileOpener;
import liquibase.exception.ChangeLogParseException;

import java.util.Map;

public interface ChangeLogParser {

    public DatabaseChangeLog parse(String physicalChangeLogLocation, Map<String, Object> changeLogParameters, FileOpener fileOpener) throws ChangeLogParseException;

    public String[] getValidFileExtensions();

    public ChangeLogSerializer getSerializer();
}
