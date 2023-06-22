package liquibase.parser;

import liquibase.Scope;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.UnknownFormatException;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.PrioritizedService;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SnapshotParserFactory {

    private static SnapshotParserFactory instance;

    private final List<SnapshotParser> parsers = new CopyOnWriteArrayList<>();


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
    public static synchronized void setInstance(SnapshotParserFactory instance) {
        SnapshotParserFactory.instance = instance;
    }

    private SnapshotParserFactory() {
        try {
            for (SnapshotParser parser : Scope.getCurrentScope().getServiceLocator().findInstances(SnapshotParser.class)) {
                    register(parser);
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

    }

    public List<SnapshotParser> getParsers() {
        return Collections.unmodifiableList(parsers);
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
        parsers.sort(PrioritizedService.COMPARATOR);
    }

    public void unregister(SnapshotParser snapshotParser) {
        parsers.remove(snapshotParser);
    }
}
