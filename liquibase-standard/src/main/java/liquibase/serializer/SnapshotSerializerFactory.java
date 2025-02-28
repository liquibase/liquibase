package liquibase.serializer;

import liquibase.Scope;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.PrioritizedService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SnapshotSerializerFactory {
    private static SnapshotSerializerFactory instance;

    private final Map<String, List<SnapshotSerializer>> serializers = new ConcurrentHashMap<>();

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
        try {
            for (SnapshotSerializer serializer : Scope.getCurrentScope().getServiceLocator().findInstances(SnapshotSerializer.class)) {
                register(serializer);
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
            List<SnapshotSerializer> snapshotSerializers = serializers.computeIfAbsent(extension, k -> new ArrayList<>());
            snapshotSerializers.add(snapshotSerializer);
            snapshotSerializers.sort(PrioritizedService.COMPARATOR);
        }
    }

    public void unregister(SnapshotSerializer snapshotSerializer) {
        for (Iterator<Map.Entry<String, List<SnapshotSerializer>>> entryIterator = serializers.entrySet().iterator(); entryIterator.hasNext();) {
            Map.Entry<String, List<SnapshotSerializer>> entry = entryIterator.next();
            List<SnapshotSerializer> snapshotSerializers = entry.getValue();
            snapshotSerializers.removeIf(value -> value.equals(snapshotSerializer));
            if (snapshotSerializers.isEmpty()) {
                entryIterator.remove();
            }
        }
    }
}
