package liquibase.serializer;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SnapshotSerializerFactory {
    private static SnapshotSerializerFactory instance;

    private Map<String, SnapshotSerializer> serializers = new HashMap<String, SnapshotSerializer>();


    public static void reset() {
        instance = new SnapshotSerializerFactory();
    }

    public static SnapshotSerializerFactory getInstance() {
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

    public Map<String, SnapshotSerializer> getSerializers() {
        return serializers;
    }

    public SnapshotSerializer getSerializer(String fileNameOrExtension) {
        fileNameOrExtension = fileNameOrExtension.replaceAll(".*\\.", ""); //just need the extension
        SnapshotSerializer snapshotSerializer = serializers.get(fileNameOrExtension);
        if (snapshotSerializer == null) {
            throw new RuntimeException("No serializer associated with the filename or extension '" + fileNameOrExtension + "'");
        }
        return snapshotSerializer;
    }

    public void register(SnapshotSerializer snapshotSerializer) {
        for (String extension : snapshotSerializer.getValidFileExtensions()) {
            serializers.put(extension, snapshotSerializer);
        }
    }

    public void unregister(SnapshotSerializer snapshotSerializer) {
        List<Map.Entry<String, SnapshotSerializer>> entrysToRemove = new ArrayList<Map.Entry<String, SnapshotSerializer>>();
        for (Map.Entry<String, SnapshotSerializer> entry : serializers.entrySet()) {
            if (entry.getValue().equals(snapshotSerializer)) {
                entrysToRemove.add(entry);
            }
        }

        for (Map.Entry<String, SnapshotSerializer> entry : entrysToRemove) {
            serializers.remove(entry.getKey());
        }

    }

}
