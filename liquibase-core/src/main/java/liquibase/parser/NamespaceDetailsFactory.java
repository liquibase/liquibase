package liquibase.parser;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.serializer.LiquibaseSerializer;
import liquibase.servicelocator.ServiceLocator;

import java.util.*;

public class NamespaceDetailsFactory {

    private static NamespaceDetailsFactory instance;

    private List<NamespaceDetails> namespaceDetails = new ArrayList<>();

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
        Class<? extends NamespaceDetails>[] classes;
        try {
            classes = ServiceLocator.getInstance().findClasses(NamespaceDetails.class);

            for (Class<? extends NamespaceDetails> clazz : classes) {
                register(clazz.getConstructor().newInstance());
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
            LogService.getLog(getClass()).debug(LogType.LOG, "No parser namespace details associated with namespace '" + namespace + "' and parser " + parser.getClass().getName());
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
            LogService.getLog(getClass()).debug(LogType.LOG, "No serializer namespace details associated with namespace '" + namespace + "' and serializer " + serializer.getClass().getName());
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
            return Integer.valueOf(o2.getPriority()).compareTo(o1.getPriority());
        }
    }
}
