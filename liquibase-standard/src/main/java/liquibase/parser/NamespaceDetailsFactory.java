package liquibase.parser;

import liquibase.Scope;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.serializer.LiquibaseSerializer;

import java.util.*;

public class NamespaceDetailsFactory {

    private static NamespaceDetailsFactory instance;

    private final List<NamespaceDetails> namespaceDetails = new ArrayList<>();

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
        SortedSet<NamespaceDetails> validNamespaceDetails = new TreeSet<>(new SerializerNamespaceDetailsComparator());

        for (NamespaceDetails details : namespaceDetails) {
            if (details.supports(parser, namespace)) {
                validNamespaceDetails.add(details);
            }
        }

        if (validNamespaceDetails.isEmpty()) {
            Scope.getCurrentScope().getLog(getClass()).fine("No parser namespace details associated with namespace '" + namespace + "' and parser " + parser.getClass().getName());
        }

        return validNamespaceDetails.iterator().next();
    }

    public NamespaceDetails getNamespaceDetails(LiquibaseSerializer serializer, String namespace) {
        SortedSet<NamespaceDetails> validNamespaceDetails = new TreeSet<>(new SerializerNamespaceDetailsComparator());

        for (NamespaceDetails details : namespaceDetails) {
            if (details.supports(serializer, namespace)) {
                validNamespaceDetails.add(details);
            }
        }

        if (validNamespaceDetails.isEmpty()) {
            Scope.getCurrentScope().getLog(getClass()).fine("No serializer namespace details associated with namespace '" + namespace + "' and serializer " + serializer.getClass().getName());
        }

        return validNamespaceDetails.iterator().next();
    }

    public void register(NamespaceDetails namespaceDetails) {
        this.namespaceDetails.add(namespaceDetails);
    }

    public void unregister(NamespaceDetails namespaceDetails) {
        this.namespaceDetails.remove(namespaceDetails);
    }

    private class SerializerNamespaceDetailsComparator implements Comparator<NamespaceDetails> {
        @Override
        public int compare(NamespaceDetails o1, NamespaceDetails o2) {
            return Integer.compare(o2.getPriority(), o1.getPriority());
        }
    }
}
