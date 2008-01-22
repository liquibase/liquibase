package liquibase.parser;

import liquibase.DatabaseChangeLog;
import liquibase.FileOpener;
import liquibase.exception.ChangeLogParseException;
import liquibase.parser.xml.XMLChangeLogParser;

public class ChangeLogParser {

    public DatabaseChangeLog parse(String physicalChangeLogLocation, FileOpener fileOpener) throws ChangeLogParseException {
        return new XMLChangeLogParser().parse(physicalChangeLogLocation, fileOpener);        
    }

}
