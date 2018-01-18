package liquibase.serializer;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.PrioritizedService;
import liquibase.servicelocator.ServiceLocator;

import java.util.*;

public class SnapshotSerializerFactory {
    private static SnapshotSerializerFactory instance;

    private Map<String, List<SnapshotSerializer>> serializers = new HashMap<>();

    public static synchronized void reset() {
        instance = new SnapshotSerializerFactory();
    }

    public static synchronized SnapshotSerializerFactory getInstance() {
        if (instance == null) {
            instance = new SnapshotSerializerFactory();
        }

        return instance;
    }

    private SnapshotSerializerFactory() {
        Class<? extends SnapshotSerializer>[] classes;
        try {
            classes = ServiceLocator.getInstance().findClasses(SnapshotSerializer.class);

            for (Class<? extends SnapshotSerializer> clazz : classes) {
                register((SnapshotSerializer) clazz.getConstructor().newInstance());
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public Map<String, List<SnapshotSerializer>> getSerializers() {
        return serializers;
    }

    public List<SnapshotSerializer> getSerializers(String fileNameOrExtension) {
        fileNameOrExtension = fileNameOrExtension.replaceAll(".*\\.", ""); //just need the extension
        List<SnapshotSerializer> snapshotSerializers = serializers.get(fileNameOrExtension);
        if (snapshotSerializers == null) {
            return Collections.emptyList();
        }
        return snapshotSerializers;
    }

    public SnapshotSerializer getSerializer(String fileNameOrExtension) {
        List<SnapshotSerializer> snapshotSerializers = getSerializers(fileNameOrExtension);
        if (snapshotSerializers.isEmpty()) {
            throw new RuntimeException("No serializers associated with the filename or extension '" + fileNameOrExtension + "'");
        }
        return snapshotSerializers.get(0);
    }

    public void register(SnapshotSerializer snapshotSerializer) {
        for (String extension : snapshotSerializer.getValidFileExtensions()) {
            List<SnapshotSerializer> snapshotSerializers = serializers.get(extension);
            if (snapshotSerializers == null) {
                snapshotSerializers = new ArrayList<>();
                serializers.put(extension, snapshotSerializers);
            }
            snapshotSerializers.add(snapshotSerializer);
            Collections.sort(snapshotSerializers, PrioritizedService.COMPARATOR);
        }
    }

    public void unregister(SnapshotSerializer snapshotSerializer) {
        for (Iterator<Map.Entry<String, List<SnapshotSerializer>>> entryIterator = serializers.entrySet().iterator(); entryIterator.hasNext();) {
            Map.Entry<String, List<SnapshotSerializer>> entry = entryIterator.next();
            List<SnapshotSerializer> snapshotSerializers = entry.getValue();
            for (Iterator<SnapshotSerializer> iterator = snapshotSerializers.iterator(); iterator.hasNext();) {
                SnapshotSerializer value = iterator.next();
                if (value.equals(snapshotSerializer)) {
                    iterator.remove();
                }
            }
            if (snapshotSerializers.isEmpty()) {
                entryIterator.remove();
            }
        }
    }
}
