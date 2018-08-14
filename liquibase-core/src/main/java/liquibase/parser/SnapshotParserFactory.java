package liquibase.parser;

import liquibase.Scope;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.UnknownFormatException;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.ServiceLocator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SnapshotParserFactory {

    private static SnapshotParserFactory instance;

    private List<SnapshotParser> parsers;
    private Comparator<SnapshotParser> snapshotParserComparator;


    public static synchronized void reset() {
        instance = new SnapshotParserFactory();
    }

    public static synchronized SnapshotParserFactory getInstance() {
        if (instance == null) {
             instance = new SnapshotParserFactory();
        }
        return instance;
    }

    /**
     * Set the instance used by this singleton. Used primarily for testing.
     */
    public static void setInstance(SnapshotParserFactory instance) {
        SnapshotParserFactory.instance = instance;
    }

    private SnapshotParserFactory() {
        snapshotParserComparator = new Comparator<SnapshotParser>() {
            @Override
            public int compare(SnapshotParser o1, SnapshotParser o2) {
                return Integer.valueOf(o2.getPriority()).compareTo(o1.getPriority());
            }
        };

        parsers = new ArrayList<>();
        try {
            for (SnapshotParser parser : Scope.getCurrentScope().getServiceLocator().findInstances(SnapshotParser.class)) {
                    register(parser);
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

    }

    public List<SnapshotParser> getParsers() {
        return parsers;
    }

    public SnapshotParser getParser(String fileNameOrExtension, ResourceAccessor resourceAccessor) throws LiquibaseException {
        for (SnapshotParser parser : parsers) {
            if (parser.supports(fileNameOrExtension, resourceAccessor)) {
                return parser;
            }
        }

        throw new UnknownFormatException("Cannot find parser that supports "+fileNameOrExtension);
    }

    public void register(SnapshotParser snapshotParser) {
        parsers.add(snapshotParser);
        Collections.sort(parsers, snapshotParserComparator);
    }

    public void unregister(SnapshotParser snapshotParser) {
        parsers.remove(snapshotParser);
    }
}
