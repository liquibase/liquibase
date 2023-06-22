package liquibase.serializer;

public class SnapshotSerializerFactory extends BaseSerializerFactory<SnapshotSerializer> {
    private static SnapshotSerializerFactory instance;

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
        super(SnapshotSerializer.class);
    }
}
