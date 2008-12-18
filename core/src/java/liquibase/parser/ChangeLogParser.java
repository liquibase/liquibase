package liquibase.parser;

import liquibase.DatabaseChangeLog;
import liquibase.FileOpener;
import liquibase.exception.ChangeLogParseException;
import liquibase.parser.xml.XMLChangeLogParser;
import liquibase.parser.sql.SqlChangeLogGenerator;

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

    public DatabaseChangeLog parse(String physicalSqlFileLocation, FileOpener fileOpener) throws ChangeLogParseException {
        if (physicalSqlFileLocation.endsWith("sql")) {
            return new SqlChangeLogGenerator().generate(physicalSqlFileLocation, fileOpener, changeLogParameters);
        }
        return new XMLChangeLogParser().parse(physicalSqlFileLocation, fileOpener, changeLogParameters);
    }

}
