package liquibase.serializer;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.servicelocator.ServiceLocator;

import java.util.*;

public class SerializerNamespaceDetailsFactory {

    private static SerializerNamespaceDetailsFactory instance;

    private List<SerializerNamespaceDetails> namespaceDetails = new ArrayList<SerializerNamespaceDetails>();

    public static void reset() {
        instance = null;
    }

    public static SerializerNamespaceDetailsFactory getInstance() {
        if (instance == null) {
            instance = new SerializerNamespaceDetailsFactory();
        }

        return instance;
    }

    private SerializerNamespaceDetailsFactory() {
        Class<? extends SerializerNamespaceDetails>[] classes;
        try {
            classes = ServiceLocator.getInstance().findClasses(SerializerNamespaceDetails.class);

            for (Class<? extends SerializerNamespaceDetails> clazz : classes) {
                register(clazz.getConstructor().newInstance());
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

    }

    public SerializerNamespaceDetails getNamespaceDetails(LiquibaseSerializer serializer, String namespace) {
        SortedSet<SerializerNamespaceDetails> validNamespaceDetails = new TreeSet<SerializerNamespaceDetails>(new SerializerNamespaceDetailsComparator());

        for (SerializerNamespaceDetails details : namespaceDetails) {
            if (details.supports(serializer, namespace)) {
                validNamespaceDetails.add(details);
            }
        }

        if (validNamespaceDetails.isEmpty()) {
            LogFactory.getInstance().getLog().debug("No serializer namespace details associated with namespace '" + namespace + "' and serializer " + serializer.getClass().getName());
        }

        return validNamespaceDetails.iterator().next();
    }

    public void register(SerializerNamespaceDetails serializerNamespaceDetails) {
        namespaceDetails.add(serializerNamespaceDetails);
    }

    public void unregister(SerializerNamespaceDetails serializerNamespaceDetails) {
        namespaceDetails.remove(serializerNamespaceDetails);
    }

    private class SerializerNamespaceDetailsComparator implements Comparator<SerializerNamespaceDetails> {
        @Override
        public int compare(SerializerNamespaceDetails o1, SerializerNamespaceDetails o2) {
            return Integer.valueOf(o2.getPriority()).compareTo(o1.getPriority());
        }
    }
}
