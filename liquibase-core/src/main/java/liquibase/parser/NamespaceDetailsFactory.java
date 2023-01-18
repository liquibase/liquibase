package liquibase.parser;

import liquibase.Scope;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.serializer.LiquibaseSerializer;
import liquibase.servicelocator.PrioritizedService;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class NamespaceDetailsFactory {

    private static NamespaceDetailsFactory instance;

    private final List<NamespaceDetails> namespaceDetails = new CopyOnWriteArrayList<>();

    public static synchronized void reset() {
        instance = null;
    }

    public static synchronized NamespaceDetailsFactory getInstance() {
        if (instance == null) {
            instance = new NamespaceDetailsFactory();
        }

        return instance;
    }

    private NamespaceDetailsFactory() {
        try {
            for (NamespaceDetails details : Scope.getCurrentScope().getServiceLocator().findInstances(NamespaceDetails.class)) {
                register(details);
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

    }

    public Collection<NamespaceDetails> getNamespaceDetails() {
        return Collections.unmodifiableCollection(namespaceDetails);
    }

    public NamespaceDetails getNamespaceDetails(LiquibaseParser parser, String namespace) {
        Optional<NamespaceDetails> details = namespaceDetails
                .stream()
                .filter(nd -> nd.supports(parser, namespace))
                .min(PrioritizedService.COMPARATOR);

        if (details.isPresent()) return details.get();

        String message = "No parser namespace details associated with namespace '" + namespace + "' and parser " + parser.getClass().getName();
        Scope.getCurrentScope().getLog(getClass()).fine(message);
        throw new NoSuchElementException(message);
    }

    public NamespaceDetails getNamespaceDetails(LiquibaseSerializer serializer, String namespace) {
        Optional<NamespaceDetails> details = namespaceDetails
                .stream()
                .filter(nd -> nd.supports(serializer, namespace))
                .min(PrioritizedService.COMPARATOR);

        if (details.isPresent()) return details.get();

        String message = "No serializer namespace details associated with namespace '" + namespace + "' and serializer " + serializer.getClass().getName();
        Scope.getCurrentScope().getLog(getClass()).fine(message);
        throw new NoSuchElementException(message);
    }

    public void register(NamespaceDetails namespaceDetails) {
        this.namespaceDetails.add(namespaceDetails);
    }

    public void unregister(NamespaceDetails namespaceDetails) {
        this.namespaceDetails.remove(namespaceDetails);
    }
}
