package liquibase.parser;

import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.UnknownChangelogFormatException;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.ServiceLocator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChangeLogParserFactory {

    private static ChangeLogParserFactory instance;

    private List<ChangeLogParser> parsers;
    private Comparator<ChangeLogParser> changelogParserComparator;


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
        Class<? extends ChangeLogParser>[] classes;
        changelogParserComparator = new Comparator<ChangeLogParser>() {
            @Override
            public int compare(ChangeLogParser o1, ChangeLogParser o2) {
                return Integer.valueOf(o2.getPriority()).compareTo(o1.getPriority());
            }
        };

        parsers = new ArrayList<>();
        try {
            classes = ServiceLocator.getInstance().findClasses(ChangeLogParser.class);

            for (Class<? extends ChangeLogParser> clazz : classes) {
                    register((ChangeLogParser) clazz.getConstructor().newInstance());
            }
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

        throw new UnknownChangelogFormatException("Cannot find parser that supports "+fileNameOrExtension);
    }

    public void register(ChangeLogParser changeLogParser) {
        parsers.add(changeLogParser);
        Collections.sort(parsers, changelogParserComparator);
    }

    public void unregister(ChangeLogParser changeLogParser) {
        parsers.remove(changeLogParser);
    }
}
