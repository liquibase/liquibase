package liquibase.parser;

import liquibase.Scope;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.UnknownChangelogFormatException;
import liquibase.resource.ResourceAccessor;

import java.util.*;

public class ChangeLogParserFactory {

    private static ChangeLogParserFactory instance;

    private List<ChangeLogParser> parsers = new ArrayList<>();

    public static synchronized void reset() {
        instance = new ChangeLogParserFactory();
    }

    public static synchronized ChangeLogParserFactory getInstance() {
        if (instance == null) {
            instance = new ChangeLogParserFactory();
        }
        return instance;
    }

    /**
     * Set the instance used by this singleton. Used primarily for testing.
     */
    public static void setInstance(ChangeLogParserFactory instance) {
        ChangeLogParserFactory.instance = instance;
    }

    private ChangeLogParserFactory() {
        try {
            List<ChangeLogParser> parser = Scope.getCurrentScope().getServiceLocator().findInstances(ChangeLogParser.class);
            register(parser);
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public List<ChangeLogParser> getParsers() {
        return parsers;
    }

    public ChangeLogParser getParser(String fileNameOrExtension, ResourceAccessor resourceAccessor) throws LiquibaseException {
        for (ChangeLogParser parser : parsers) {
            if (parser.supports(fileNameOrExtension, resourceAccessor)) {
                return parser;
            }
        }

        throw new UnknownChangelogFormatException("Cannot find parser that supports " + fileNameOrExtension);
    }

    public void register(ChangeLogParser changeLogParsers) {
        register(Collections.singletonList(changeLogParsers));
    }

    private void register(List<ChangeLogParser> changeLogParsers) {
        parsers.addAll(changeLogParsers);
        parsers.sort(ChangeLogParser.COMPARATOR);
    }

    public void unregister(ChangeLogParser changeLogParser) {
        parsers.remove(changeLogParser);
    }
}
