package liquibase.serializer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChangeLogSerializerFactory extends BaseSerializerFactory<ChangeLogSerializer> {
    private static ChangeLogSerializerFactory instance;

    private Map<String, List<ChangeLogSerializer>> serializers = new ConcurrentHashMap<>();

    public static synchronized void reset() {
        instance = new ChangeLogSerializerFactory();
    }

    public static synchronized ChangeLogSerializerFactory getInstance() {
        if (instance == null) {
            instance = new ChangeLogSerializerFactory();
        }

        return instance;
    }

    private ChangeLogSerializerFactory() {
        super(ChangeLogSerializer.class);
    }

    Map<String, List<ChangeLogSerializer>> getSerializers() {
        return serializers;
    }
}
