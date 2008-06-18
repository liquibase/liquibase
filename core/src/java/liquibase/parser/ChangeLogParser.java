package liquibase.parser;

import liquibase.DatabaseChangeLog;
import liquibase.FileOpener;
import liquibase.exception.ChangeLogParseException;
import liquibase.parser.xml.XMLChangeLogParser;

import java.util.HashMap;
import java.util.Map;

public class ChangeLogParser {

    private Map<String, Object> changeLogParameters;

    public ChangeLogParser() {
        this.changeLogParameters = new HashMap<String, Object>();
    }

    public ChangeLogParser(Map<String, Object> changeLogParameters) {
        this.changeLogParameters = changeLogParameters;
    }

    public DatabaseChangeLog parse(String physicalChangeLogLocation, FileOpener fileOpener) throws ChangeLogParseException {
        return new XMLChangeLogParser().parse(physicalChangeLogLocation, fileOpener, changeLogParameters);
    }

}
